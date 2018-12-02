package eShop

import java.time.Duration

import akka.actor.{Actor, Timers}
import akka.event.LoggingReceive
import eShop.Commands.Cart._
import eShop.Events.Cart._
import eShop.Commands.Checkout.{StartCheckout => CheckoutStartCheckout}
import eShop.Data.CartInfo

import scala.collection.mutable

class Cart extends Actor with Timers {
  val items: mutable.Queue[String] = mutable.Queue[String]()

  def receive: Receive = empty

  def empty: Receive = LoggingReceive {
    case AddItem(id) =>
      items += id
      self ! ItemAdded(id)

    case ItemAdded(id) =>
      println("empty: Added item " + id)
      timers.startSingleTimer(self, ExpireTimer(), Duration.ofSeconds(1))
      context become nonEmpty

    case RemoveItem(id) =>
      println( "Cannot remove item in empty cart")

    case other =>
      println( "Cannot perform action " + other + " when cart is empty")
  }

  def nonEmpty: Receive = LoggingReceive {
    case AddItem(id) =>
      items += id
      self ! ItemAdded(id)

    case ItemAdded(id) =>
      println("nonempty: Added item " + id)

    case RemoveItem(id) =>
      val res = items.dequeueFirst(name => name == id)
      if (res.isEmpty) {
        println( "Item " + id + " is not in cart")
      } else {
        self ! ItemRemoved(id)
      }

    case ItemRemoved(id) =>
      println("Removed item " + id)
      if (items.isEmpty) {
        context become empty
      }

    case StartCheckout(checkoutRef) =>
      checkoutRef ! CheckoutStartCheckout(CartInfo(self, items.toList))
      self ! CheckoutStarted

    case CheckoutStarted =>
      println("Started checkout")
      context become inCheckout

    case ExpireTimer() =>
      println("cart Timer expired")
      items.dequeueAll(_ => true)
      context become empty

    case other =>
      println( "Cannot perform action " + other + " when cart is nonEmpty")
  }

  def inCheckout: Receive = LoggingReceive {
    case CancelCheckout() =>
      self ! CheckoutCancelled

    case CheckoutCancelled =>
      println("cart: Checkout cancelled")
      context become nonEmpty

    case CloseCheckout() =>
      items.dequeueAll(_ => true)
      self ! CheckoutClosed

    case CheckoutClosed =>
      println("cart: Checkout closed")
      context become closed
  }
  def closed: Receive = LoggingReceive {
    case _ =>
  }
}
