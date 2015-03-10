# Prerequisites

To build the project you need the following:

 - JDK 1.7
 - Maven (>= 3.1.1)

# Continuous Integration Configuration

Continuous Integration platform has to be configured to run
`mvn deploy` on every commit. Besides that the platform has to
have `settings.xml`, `pubring.gpg`, and `secring.gpg` files available
near the project source code base. These files are not inside Git
repository for security reasons. They have to be provided by the CI
platform owner (the project owner, in most cases).

`settings.xml` file has to document connection
settings to the sonatype repository
and web deployment platform. For example (this is a complete example
of `setting.xml` file):

```xml
<settings>
 <profiles>
  <profile>
   <id>rexsl</id>
   <properties>
    <gpg.homedir>../../closures/</gpg.homedir>
    <gpg.keyname>...</gpg.keyname>
    <gpg.passphrase>....</gpg.passphrase>
   </properties>
  </profile>
 </profiles>
 <servers>
  <server>
   <id>www.rexsl.com</id>
   <username>....</username>
   <password>....</password>
  </server>
  <server>
   <id>oss.sonatype.org</id>
   <username>....</username>
   <password>....</password>
  </server>
 </servers>
</settings>
```

At the moment the following goal is defined in our CI server:

```
mvn clean deploy -e -Dci --settings ../settings.xml
```

# How to release new version to Maven Central

Create new branch

```
git checkout -b r
```

Set new version

```
mvn versions:set
git commit -am 'version set to 0.4.11'
```

Update all versions

```
mvn versions:use-latest-versions
```

Update all `changes/changes.xml` files

Commit your changes

```
git commit -am 'versions updates'
```

Deploy to sonatype:

```
rm -rf ~/.m2/repository/com/rexsl
mvn -Prexsl -Psonatype clean deploy
```

Make sure that you have this profile in `~/.m2/settings.xml`:

```xml
<profile>
  <id>rexsl</id>
  <properties>
    <gpg.homedir>/code/gpg/rexsl</gpg.homedir>
    <gpg.keyname>...</gpg.keyname>
    <gpg.passphrase>...</gpg.passphrase>
  </properties>
</profile>
```

Copy your branch to tags:

```
git tag -a rexsl-0.4.11 -m 'version 0.4.11 released to Maven Central'
git push origin rexsl-0.4.11
```

Set version back to SNAPSHOT:

```
mvn versions:set
git commit -am 'version back to SNAPSHOT'
```

Merge the branch back to master.

```
git checkout master && git merge r && git push origin master && git branch -D r
```

Deploy site to Amazon S3 (in approx 4 hours):

```
git checkout rexsl-0.4.11
mvn -Psite -Prexsl clean install site-deploy
```

Announce the release in
[Google Groups](https://groups.google.com/forum/?fromgroups#!forum/rexsl)

Write short release notes in
[Github](https://github.com/yegor256/rexsl/releases)

Announce the release in Twitter/Facebook/everywhere :)

That's it
