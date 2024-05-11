## Discord Bot v2

Showcase of using Kord and AWS CDK to build and package a discord chat bot with a suite of commands. This is a 
continuation of https://github.com/kadahlin/RuleDiscordBot. The initial repo was made years ago with custom wrappers
around Discord4J and using the bazel build system. Since initially making this project, Kord had matured enough
to easily warrant using it over my own wrappers. Bazel has also undergone large scale changes with the deprecation
of the WORKSPACE file. Considering both of these I decided to remake the bot instead of migrate the old repository.

WIP:
- GRPC configuration endpoints
- More features