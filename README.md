# Embold scanner-sync
Embold scanner update library for remote scan execution workflows

![build](https://github.com/embold/scanner-sync/workflows/build/badge.svg?branch=development)
[![Maven Central](https://img.shields.io/maven-central/v/io.embold.scan/scanner-sync.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.embold.scan%22%20AND%20a:%22scanner-sync%22)


This library provides base functionality for managing Embold Scanner version on e.g. a build machine, and automatically updating it to latest based on the version available on the Embold Server.

It finds it use in other components such as the [Embold Maven Plugin](https://github.com/embold/embold-maven-plugin)

## Usage

class `SyncSession` provides the API to manage the Embold scanner versions. It takes `SyncOpts` as argument which provides settings such as Embold URL, token, download directory, etc.