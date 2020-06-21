![Scale](src/main/resources/logo.png)
====

> A clever exaggeration of apps-in-a-snap.
>
> _Noun_:  A type of alcoholic beverage that may take several forms.   
> _Origin_: derived from the colloquial German word Schnaps [/ʃnɑːps/](https://en.wikipedia.org/wiki/Help:IPA/Standard_German)   

Schnapps is a precise toolkit to rapidly develop production-grade, self-contained WebApps and REST APIs over HTTP and WebSockets.
It is completely written in `Scala` and is compatible with `Java` as well.  

Backend developers spend a big chunk of their precious time on redundant activities across projects like app 
configuration & its management, connectivity to external systems, integration of auth mechanisms, app packaging, etc. 
They get bound to excessively complex tools/ frameworks, and eventually, these tools drive the development and not the other way around. 

Schnapps provides the boilerplate code for all these activities so that developers can focus on what matters the most - **Application Logic**.  

## Features
 - Contains an embedded server that hosts mostly managed web services
 - Provides standardized JSON-based app configuration out-of-the-box 
 - Allows Synchronous/ Asynchronous Web Applications and REST APIs
 - WebSockets, Server-Sent Events (SSE), Long-Polling, HTTP Streaming (Forever frame) and JSONP are all supported
 - Inbuilt authentication and authorization using [Apache Shiro](https://shiro.apache.org/)
 - Managed connectors to external systems like LDAP, JDBC and Redis.

## ~~Quick~~ Schnap Start
Getting started with Schnapps is as straight-forward as it can get, as exactly 3 things need to be done,
 - Get it 
 - Configure it
 - Use it 

### Get Schnapps
To get Schnapps, you need to add its dependency according to your build tool.

For Maven based projects,
```$xslt

```

For sbt based projects,
```$xslt

```

For Gradle based projects,
```$xslt

```

### Use Schnapps
Depending on whether you use Schnapps in `Scala` or `Java` project, the usage syntax changes ever so slightly.

##### Configuring the project

##### Establishing connection to Externals

In `Java` projects,
```$xslt

```

In `Scala` projects,
```$xslt

```

##### Writing your first service

In `Java` projects,
```$xslt

```

In `Scala` projects,
```$xslt

```

##### Running the Server

In `Java` projects,
```$xslt

```

In `Scala` projects,
```$xslt

```

### Advanced Topics

You can head over to the Wiki for more advanced topics like,
 - Configuring Authentication
 - Configuring Authorization
 - Configuring the Server
 - Slim ORM 
 - Integrating with Service Discovery
 - Implementing Custom externals 
 
### Contributing

We're interested in building the community and would welcome any thoughts, suggestions and/ or patches. 
You can reach us [here](mailto:chitralverma@gmail.com).


### License
Licensed under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0).

Copyright 2020 Chitral Verma

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
