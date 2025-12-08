cats-mtl-example-app
==================
[![Build-All](https://github.com/kevin-lee/cats-mtl-example/actions/workflows/build.yml/badge.svg)](https://github.com/kevin-lee/cats-mtl-example/actions/workflows/build.yml)
[![Check-All](https://github.com/kevin-lee/cats-mtl-example/actions/workflows/checks.yml/badge.svg)](https://github.com/kevin-lee/cats-mtl-example/actions/workflows/checks.yml)
[![Launch Scala Steward](https://github.com/kevin-lee/cats-mtl-example/actions/workflows/scala-steward.yml/badge.svg)](https://github.com/kevin-lee/cats-mtl-example/actions/workflows/scala-steward.yml)

How to Run
----------
```bash
sbt run
```

Once you get log messages like this
```sbtshell
[info] running io.kevinlee.http4sexampleapp.MainServer
[ioapp-compute-0] INFO  o.h.b.c.n.NIO1SocketServerGroup - Service bound to address /0:0:0:0:0:0:0:0:8080
[ioapp-compute-0] INFO  o.h.b.s.BlazeServerBuilder -
  _   _   _        _ _
 | |_| |_| |_ _ __| | | ___
 | ' \  _|  _| '_ \_  _(_-<
 |_||_\__|\__| .__/ |_|/__/
             |_|
[ioapp-compute-0] INFO  o.h.b.s.BlazeServerBuilder - http4s v0.22.8 on blaze v0.15.2 started at http://[::]:8080/
```
***
In development, `reStart` and `reStop` are recommended to start and stop the app.

## Index Page

Access [http://localhost:8080/html/index.html](http://localhost:8080/html/index.html) to access an example static html page. It's handled by `StaticHtmlService`.

```
http://localhost:8080/html/index.html
```

## API

### `hello world`

Access [http://localhost:8080/hello](http://localhost:8080/hello) in your web browser. It will give the following JSON.
```
http://localhost:8080/hello
```
```json
{
  "result": "Hello, World!"
}
```

### `hello NAME`

Access [http://localhost:8080/hello/YOUR_NAME](http://localhost:8080/hello/YOUR_NAME) in your web browser. It will give the following JSON.
```
http://localhost:8080/hello/YOUR_NAME
```
```json
{
  "result": "Hello, YOUR_NAME"
}
``` 

### `add Int + Int`

Access [http://localhost:8080/hello/add](http://localhost:8080/hello/add) with two numbers in the path.
e.g.) [http://localhost:8080/hello/add/2/5](http://localhost:8080/hello/add/2/5)

```
http://localhost:8080/hello/add/2/5
```

Result:
```json
{
  "result": 7
}
```

