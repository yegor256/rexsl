<img src="http://img.rexsl.com/logo-200x45.png" />

The idea is simple - to make a RESTful XML API available in a browser-friendly
format renderable with XSL stylesheets. With ReXSL you develop your web
application as a RESTful "web service" while XSL stylesheets transform it to an HTML web site.

Read more at [www.rexsl.com](http://www.rexsl.com).

For example, these sites use ReXSL:
[s3auth.com](http://www.s3auth.com),
[expinia.com](http://p.expinia.com),
[netbout.com](http://www.netbout.com),
[rultor.com](http://www.rultor.com),
[bibrarian.com](http://www.bibrarian.com).

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
a new [Github issue](https://github.com/yegor256/rexsl/issues/new),
or a question to our
[Google Group](https://groups.google.com/forum/#!forum/rexsl).
