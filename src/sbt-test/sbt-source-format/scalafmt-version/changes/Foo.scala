package com.swoval;

object Foo {
  List(1).map(x => x.map(identity).map(identity).map(identity))
}
