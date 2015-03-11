<img src="http://img.rexsl.com/logo-200x45.png" />

[![Made By Teamed.io](http://img.teamed.io/btn.svg)](http://www.teamed.io)
[![DevOps By Rultor.com](http://www.rultor.com/b/yegor256/rexsl)](http://www.rultor.com/p/yegor256/rexsl)

[![Build Status](https://travis-ci.org/yegor256/rexsl.svg?branch=master)](https://travis-ci.org/yegor256/rexsl)
[![Build status](https://ci.appveyor.com/api/projects/status/qfgwopxasua7xtwg/branch/master?svg=true)](https://ci.appveyor.com/project/yegor256/rexsl/branch/master)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.rexsl/rexsl/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.rexsl/rexsl)

**ATTENTION:** This project is deprecated. Its successor is [takes.org](http://www.takes.org).

The idea is simple - to make a RESTful XML API available in a browser-friendly
format renderable with XSL stylesheets. With ReXSL you develop your web
application as a RESTful "web service" while XSL stylesheets transform it to an HTML web site.

Read more at [www.rexsl.com](http://www.rexsl.com).

For example, this site uses ReXSL:
[s3auth.com](http://www.s3auth.com).

## How to contribute

Fork repository, make changes, send us a pull request. We will review
your changes and apply them to the `master` branch shortly, provided
they don't violate our quality standards. To avoid frustration, before
sending us your pull request please run full Maven build:

```
$ mvn clean install -Pqulice
```

## Got questions?

If you have questions or general suggestions, don't hesitate to submit
a new [Github issue](https://github.com/yegor256/rexsl/issues/new).
