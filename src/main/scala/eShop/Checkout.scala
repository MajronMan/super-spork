package eShop

import java.time.Duration

import akka.actor.{Actor, ActorRef, Timers}
import akka.event.LoggingReceive
import eShop.Commands.Checkout._
import eShop.Events.Checkout._

object Checkout {
  val checkoutTimerId = "CheckoutTimerId"
  val paymentTimerId = "PaymentTimerId"
}

class Checkout extends Actor with Timers {
  var cartRef: ActorRef = _
  var itemIds: List[String] = _

  def receive: Receive = begin

  def begin: Receive = LoggingReceive {
    case StartCheckout(cartInfo) =>
      this.cartRef = cartInfo.cartRef
      this.itemIds = cartInfo.itemIds
      timers.startSingleTimer(Checkout.checkoutTimerId, ExpireCheckoutTimer(), Duration.ofSeconds(2))
      self ! CheckoutStarted

    case CheckoutStarted =>
      context become selectingDelivery

    case _ => println("Need to initialize checkout first")
  }

  def selectingDelivery: Receive = LoggingReceive {
    case CancelCheckout() =>
      cartRef ! CancelCheckout()
      self ! CheckoutCancelled

    case SelectDeliveryMethod(id) =>
      self ! DeliveryMethodSelected(id)

    case DeliveryMethodSelected(id) =>
      println("Selected delivery method " + id)
      timers.startSingleTimer(Checkout.paymentTimerId, ExpirePaymentTimer(), Duration.ofSeconds(1))
      context become selectingPaymentMethod

    case ExpireCheckoutTimer() =>
      println("Checkout timer expired")
      self ! CancelCheckout()

    case other =>
      println("Cannot perform action " + other + " when selecting delivery")
  }

  def cancelled: Receive = LoggingReceive {
    case other =>
      println("Cannot perform action " + other + " when cancelled")
  }

  def selectingPaymentMethod: Receive = LoggingReceive {
    case CancelCheckout() =>
      cartRef ! CancelCheckout()
      self ! CheckoutCancelled()

    case CheckoutCancelled() =>
      println("Checkout cancelled")
      context become cancelled

    case SelectPaymentMethod(id) =>
      self ! PaymentSelected(id)

    case ExpireCheckoutTimer() =>
      println("Checkout timer expired")
      self ! CancelCheckout()

    case ExpirePaymentTimer() =>
      println("Payment timer expired")
      self ! CancelCheckout()

    case PaymentSelected(id) =>
      println("Selected payment " + id)
      timers.cancel(Checkout.checkoutTimerId)
      context become processingPayment
  }

  def processingPayment: Receive = LoggingReceive {
    case CancelCheckout() =>
      cartRef ! CancelCheckout()
      self ! CheckoutCancelled()

    case CheckoutCancelled() =>
      println("Checkout cancelled")
      context become cancelled

    case ReceivePayment() =>
      timers.cancel(Checkout.paymentTimerId)
      self ! PaymentReceived()

    case PaymentReceived() =>
      println("Payment received")
      cartRef ! eShop.Commands.Cart.CloseCheckout()
      context become closed
  }

  def closed: Receive = LoggingReceive {
    case _ => None
  }
}
