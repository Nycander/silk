---
layout : doc
title : About
categories : [general]
---
# About

<abstract>
The name _Silk_ is chosen because its strings are elegant, lightweight, thin and transparent but still reliable and robust. 
So silk as a material matches what we are looking for when connecting software components like we do with DI.
</abstract>

## Me <small style="font-weight:normal;">(the author)</small>
I am a software developer living in Växjö, Sweden. I coded the Java, did the website, logo and everything else connected to the project during my spare time in the last couple of month.

## This Website
This site is hosted on <a href="http://pages.github.com/">githup pages</a>. It uses <a href="http://jekyllbootstrap.com/">Jekyll</a> that is building static HTML pages from <a href="http://daringfireball.net/projects/markdown/">markdown</a> files on every <a href="http://git-scm.com/">git</a> push I do. All <span class="icon-eye-open"></span> icons are inserted using the <a href="http://fortawesome.github.io/Font-Awesome/">Font-Awesome</a> font.

For development a local Jekyll is used having this `_config.yaml` (mostly stated for me):

	source:      .
	destination: ./_site
	auto:        true
	server:      true
	baseurl:     /

	markdown:    rdiscount
	pygments:    true

### Javadoc Autolinking
A lot of Java type names are linked to the javadoc of that type (example: `Binder`). Those links are not contained in the page source. There they are a `<code></code>` block created by the normal markup for inline-code.
The below <a href="http://jquery.com/">jQuery</a> script fetches a JSON description of known types created by a Java-Doclet and transforms the blocks into the links you see.

The full script can be viewed <a href="/assets/js/autolink.js">here</a>.
