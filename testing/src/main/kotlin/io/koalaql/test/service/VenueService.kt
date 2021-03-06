package io.koalaql.test.service

import io.koalaql.dsl.*
import io.koalaql.jdbc.JdbcDataSource
import io.koalaql.test.models.*
import io.koalaql.test.table.ReviewTable
import io.koalaql.test.table.UserTable
import io.koalaql.test.table.UserVenueTable
import io.koalaql.test.table.VenueTable
import io.koalaql.transact

class VenueService(
    private val db: JdbcDataSource,
    private val onConflict: OnConflictSupport
) {
    init {
        db.declareTables(UserTable, VenueTable, UserVenueTable, ReviewTable)
    }

    fun createVenues(venues: List<NewVenue>): List<Int> = db.transact { cxn ->
        VenueTable
            .insert(values(venues) {
                set(VenueTable.name, it.name)
                set(VenueTable.description, it.description)
                set(VenueTable.closedPermanently, it.closed)
                set(VenueTable.type, it.type)
            })
            .generatingKey(VenueTable.id)
            .perform(cxn)
            .toList()
    }

    fun fetchVenues(ids: List<Int>? = null): List<Venue> {
        return db.transact { cxn ->
            val visits = label<Int>()
            val visitCte = cte()

            val rows = VenueTable
                .leftJoin(visitCte, UserVenueTable.venue eq VenueTable.id)
                .let {
                    if (ids != null) it.where(VenueTable.id inValues ids)
                    else it
                }
                .orderBy(VenueTable.id)
                .select(VenueTable, coalesce(visits, value(0)) as_ visits)
                .with(visitCte as_ UserVenueTable
                    .where(UserVenueTable.visited)
                    .groupBy(UserVenueTable.venue)
                    .select(UserVenueTable.venue, count(value(1)) as_ visits)
                )
                .perform(cxn)
                .toList()

            val reviewsByVenue = ReviewTable
                .where(ReviewTable.venue inValues rows.map { it[VenueTable.id] })
                .orderBy(ReviewTable.venue, ReviewTable.created.desc(), ReviewTable.user)
                .selectAll()
                .perform(cxn)
                .groupBy({ it[ReviewTable.venue] }) { row ->
                    VenueReview(
                        user = row[ReviewTable.user],
                        created = row[ReviewTable.created],
                        edited = row[ReviewTable.edited],
                        content = row[ReviewTable.contents]
                    )
                }

            rows.map { row ->
                Venue(
                    id = row[VenueTable.id],
                    created = row[VenueTable.created],
                    name = row[VenueTable.name],
                    description = row[VenueTable.description],
                    closed = row[VenueTable.closedPermanently],
                    rating = row[VenueTable.rating],
                    type = row[VenueTable.type],
                    visits = row.getValue(visits),
                    reviews = reviewsByVenue[row[VenueTable.id]].orEmpty()
                )
            }
            .toList()
        }
    }

    fun mergeReviews(reviews: List<NewReview>) {
        db.transact { cxn ->
            ReviewTable
                .insert(values(reviews.map {
                    rowOf(
                        ReviewTable.user setTo it.user,
                        ReviewTable.venue setTo it.venue,
                        ReviewTable.contents setTo it.content
                    )
                }))
                .perform(cxn)
        }
    }

    fun deleteReviews(reviews: List<ReviewKey>) {
        db.transact { cxn ->
            val deleted = alias() as_ values(reviews) {
                set(ReviewTable.user, it.user)
                set(ReviewTable.venue, it.venue)
            }

            ReviewTable
                .where(exists(deleted
                    .where(deleted[ReviewTable.venue] eq ReviewTable.venue)
                    .where(deleted[ReviewTable.user] eq ReviewTable.user)
                    .select(deleted[ReviewTable.venue])
                ))
                .delete()
                .with(deleted)
                .perform(cxn)
        }
    }

    fun updateVisits(
        updates: List<VenueVisitorUpdate>
    ) {
        if (updates.isEmpty()) return

        fun Iterable<VenueVisitorUpdate>.toValues() = values(this) {
            set(UserVenueTable.venue, it.venue)
            set(UserVenueTable.user, it.user)
            set(UserVenueTable.visited, it.state)
        }

        db.transact { cxn ->
            val values = updates.toValues()

            val finalValues = if (onConflict == OnConflictSupport.NONE) {
                val valuesCte = alias() as_ values

                val alreadyExist = UserVenueTable
                    .innerJoin(valuesCte, (UserVenueTable.user eq valuesCte[UserVenueTable.user])
                        .and(UserVenueTable.venue eq valuesCte[UserVenueTable.venue])
                    )
                    .select(UserVenueTable.user, UserVenueTable.venue)
                    .with(valuesCte)
                    .perform(cxn)
                    .map { Pair(it[UserVenueTable.user], it[UserVenueTable.venue]) }
                    .toSet()

                val (needsUpdate, needsInsert) = updates.partition {
                    Pair(it.user, it.venue) in alreadyExist
                }

                if (needsUpdate.isNotEmpty()) {
                    val updateCte = alias() as_ needsUpdate.toValues()

                    val updateSubquery = updateCte
                        .where(UserVenueTable.user eq updateCte[UserVenueTable.user])
                        .where(UserVenueTable.venue eq updateCte[UserVenueTable.venue])
                        .select(updateCte[UserVenueTable.visited])

                    UserVenueTable
                        .where(exists(updateSubquery))
                        .update(UserVenueTable.visited setTo updateSubquery)
                        .with(updateCte)
                        .perform(cxn)
                }

                if (needsInsert.isEmpty()) return

                needsInsert.toValues()
            } else {
                values
            }

            val insert = UserVenueTable
                .insert(finalValues)

            val withOnConflict = when (onConflict) {
                OnConflictSupport.ON_DUPLICATE -> insert
                    .onDuplicate()
                    .set(UserVenueTable.visited)
                OnConflictSupport.ON_CONFLICT -> insert
                    .onConflict(UserVenueTable.primaryKey!!)
                    .set(UserVenueTable.visited)
                OnConflictSupport.NONE -> insert
            }

            withOnConflict.perform(cxn)
        }
    }

    fun fetchVisits(
        users: List<String>? = null,
        venues: List<Int>? = null,
    ): Set<UserVenueKey> = db.transact { cxn ->
        UserVenueTable
            .whereOptionally(users?.let { UserVenueTable.user inValues users })
            .whereOptionally(venues?.let { UserVenueTable.venue inValues venues })
            .where(UserVenueTable.visited)
            .select(UserVenueTable.user, UserVenueTable.venue)
            .perform(cxn)
            .map {
                UserVenueKey(
                    user = it[UserVenueTable.user],
                    venue = it[UserVenueTable.venue]
                )
            }
            .toSet()
    }
}