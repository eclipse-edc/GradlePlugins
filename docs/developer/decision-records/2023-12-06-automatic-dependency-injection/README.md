# Automatic Dependency Injection

## Decision

We will stop injection production dependency in the modules with the build plugin.

## Rationale

Automatic dependency injection will add potentially unnecessary dependencies to every module that uses the build plugin.

## Approach

Remove the automatic dependency injection for production dependency. The injection will be left for the test ones.
