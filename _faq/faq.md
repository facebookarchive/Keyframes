---
layout: top-level
title: FAQ
id: faq
category: faq
---

#  FAQs

**[AE Feature] is not working, why?**

To keep Keyframes simple, we don't (and don't plan to) support the full suite of features in After Effects.  This means there are a number of [constraints and guidelines]({{ '/docs/ae/guidelines/' | relative_url }}) for working on AE compositions intended for use with Keyframes.  That being said, the team loves to hear about features important for animator workflows, and we're always working to expand the list of supported features.  If you need a feature, you can open an issue on [GitHub](https://github.com/facebookincubator/Keyframes), or contribute to the project directly!

# Troubleshooting

**Exporter script hangs on "Exporting" step forever.**

This is currently a known issue we have on certain versions of AE (seems to be common for 2017).  There is an [open issue](https://github.com/facebookincubator/Keyframes/issues/28) tracking this.  For now, we recommend you to try and use the [command line version of the exporter]({{ '/docs/ae/exporting/' | relative_url }}) while we work on a fix.
