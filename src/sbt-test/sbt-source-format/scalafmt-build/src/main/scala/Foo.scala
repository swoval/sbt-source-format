package com.swoval

object Foo {
  List(List(1)).map(
    x =>
      x.map(identity(_))
        .map(identity(_))
        .map(identity(_))
  )
}
