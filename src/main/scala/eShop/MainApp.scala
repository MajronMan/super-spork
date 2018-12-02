package eShop

import akka.actor.{Actor, ActorSystem, Props}

import scala.concurrent.Await
import eShop.Commands.Cart.{AddItem, RemoveItem, StartCheckout => CartStartCheckout}
import eShop.Commands.Checkout.{ReceivePayment, SelectDeliveryMethod, SelectPaymentMethod}

import scala.concurrent.duration.Duration
import java.time.{Duration => D}

class Tester extends Actor {
  override def receive: PartialFunction[Any, Unit] = {
    case "test" => test()
    case "testFSM" => testFSM()
    case "testTimeouts" => testTimeouts()
    case other => println("Tester received: " + other)
  }
  def test(): Unit ={
    val cartActor = context.system.actorOf(Props[Cart], "cartActor")
    val checkoutActor = context.system.actorOf(Props[Checkout], "checkoutActor")
    cartActor ! AddItem("item1")
    Thread.sleep(100)
    cartActor ! AddItem("item1")
    Thread.sleep(100)
    cartActor ! RemoveItem("item1")
    Thread.sleep(100)
    cartActor ! CartStartCheckout(checkoutActor)
    Thread.sleep(100)
    checkoutActor ! SelectDeliveryMethod("post")
    Thread.sleep(100)
    checkoutActor ! SelectPaymentMethod("card")
    Thread.sleep(100)
    checkoutActor ! ReceivePayment()
  }
  def testFSM(): Unit ={
    val cartActor = context.system.actorOf(Props(classOf[CartFSM], D.ofSeconds(1)), "cartFSMActor")
    val checkoutActor = context.system.actorOf(Props(classOf[CheckoutFSM], D.ofSeconds(1), D.ofSeconds(1)), "checkoutFSMActor")

    cartActor ! AddItem("item1")
    cartActor ! AddItem("item1")
    cartActor ! RemoveItem("item1")
    cartActor ! CartStartCheckout(checkoutActor)
    Thread.sleep(100)
    checkoutActor ! SelectDeliveryMethod("post")
    checkoutActor ! SelectPaymentMethod("card")
    checkoutActor ! ReceivePayment()
  }
  def testTimeouts(): Unit ={
    val cartActor = context.system.actorOf(Props(classOf[CartFSM], D.ofSeconds(1)), "cartFSMTimeoutsActor")
    val checkoutActor = context.system.actorOf(Props(classOf[CheckoutFSM], D.ofSeconds(1), D.ofSeconds(1)), "checkoutFSMTimeoutsActor")

    cartActor ! AddItem("lol")
    Thread.sleep(2000)
    cartActor ! AddItem("xd")
    cartActor ! CartStartCheckout(checkoutActor)
    checkoutActor ! SelectDeliveryMethod("post")
    Thread.sleep(3000)
  }
}

object MainApp extends App {
  val system = ActorSystem("Main")
  val tester = system.actorOf(Props[Tester], "tester")
  tester ! "test"
  Thread.sleep(1000)
  println("\n==============================================\n")
  tester ! "testFSM"
  Thread.sleep(1000)
  println("\n==============================================\n")
  tester ! "testTimeouts"
  Await.result(system.whenTerminated, Duration.Inf)
}
