# GrChaos (Groovy Chaos Appliction Framework)

GrChaos is a powerful runtime **howswap** groovy application framework. It is a huge upgrade of [chaopeng/groovy-hotswap-demo](https://github.com/chaopeng/groovy-hotswap-demo).

## How to use it?

1. download [GrChaos-Starter](https://github.com/chaopeng/grchaos/raw/master/grchaos-starter/grchaos-starter-1.0.0-rc.zip)
2. add `bin/grchaos-starter` to $PATH
3. run `grchaos-starter` in the dir you want to create a GrChaos App
4. add GrChaos Module Class, [Example](https://github.com/chaopeng/grchaos-sample/blob/master/grchaos-sample-lib/src/main/groovy/me/chaopeng/sample/WebServiceModule.groovy)
5. add Module Class full classname to `projectName-lib/src/main/resources/application.conf`
6. run `gradle distZip` to build the project. `application.conf` is not include in the build, you can use program param to define a different configure in production 

Here is a [sample project](https://github.com/chaopeng/grchaos-sample) to show how to build a howswapable webservice. 

## GrChaos App Project layout

```
.
├── build.gradle # root build script
├── libs.gradle # libraries
├── settings.gradle # subproject define
├── projectName-app # reloadable sources, project depends on projectName-lib
└── projectName-lib # not reloadable sources
```

