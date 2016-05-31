# Chaos Framework for Groovy

chaos4g(Chaos Framework for Groovy) is a framework designed to make a groovy application support hotswap. 

------------------------

TODO

------------------------

## Motivation

I used GroovyClassLoader to hotswap java classes for java projects. But it has a lot of limitations to make the code hotswapable, only a few class is hotswapable and I need to do a lot of tricky stuff. Here is the [example](https://github.com/chaopeng/groovy-hotswap-demo). Lets see the limitations:

- hotswapable classes stores in GroovyClassLoader, we can only call the methods via reflection.
- if we can make the native call the interface would not be changable. 

I expect it can: 

- native access methods and properties
- interface changable
- base on java or groovy
- can have strong type system, at lease at coding stage

It looks impossible. 

## How it works

- a simple IoC/AOP framework to replace Spring, and support replace beans in runtime
- 2 ways to compile: 

  1. compile as normal, it can figure out a lot of problem at compile stage
  2. load classes by GroovyClassLoader, before we do it traversal the AST of class and replace type with `def`

## How to use

TODO