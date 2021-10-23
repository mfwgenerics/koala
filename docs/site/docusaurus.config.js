// @ts-check
// Note: type annotations allow type checking and IDEs autocompletion

const lightCodeTheme = require('prism-react-renderer/themes/github');
const darkCodeTheme = require('prism-react-renderer/themes/dracula');

/** @type {import('@docusaurus/types').Config} */
const config = {
  title: 'Koala Docs',
  url: 'https://mfwgenerics.github.io/',
  baseUrl: '/koala/',
  onBrokenLinks: 'throw',
  onBrokenMarkdownLinks: 'warn',
  organizationName: 'mfwgenerics',
  projectName: 'koala',
  trailingSlash: false,

  presets: [
    [
      '@docusaurus/preset-classic',
      ({
        docs: {
          routeBasePath: '/',
          sidebarPath: require.resolve('./sidebars.js'),
          editUrl: 'https://github.com/mfwgenerics/koala/blob/examples/docs/src/main/kotlin/io/koalaql/',
        },
      }),
    ],
  ],

  themeConfig:
    ({
      navbar: {
        title: 'Koala Docs',
        items: [],
      },
      prism: {
        theme: lightCodeTheme,
        darkTheme: darkCodeTheme,
        additionalLanguages: ['kotlin'],
      },
    }),
};

module.exports = config;
