package playground

object DefaultParmsDemo extends App {
  
  
  trait X {
    def foo(context: String, x: Int = 7) = {
      println(context + " :: X :: x = " + x)
    }
  }
  
  case object Y extends X
  case object Z extends X {
    override def foo(context: String, x: Int) = {
      println(context + " :: Z :: x = " + x)
    }    
  }
  
  case object P extends X {
    override def foo(context: String, x: Int = 8) = {
      println(context + " :: P :: x = " + x)
    }    
  }
  
  val xY: X = Y
  val xZ: X = Z
  val xP: X = P
  
  xY.foo("xY -> 7")
  xZ.foo("xY -> ?")
  xP.foo("xP -> ?")
  
  Z.foo("straight Z")
  
}