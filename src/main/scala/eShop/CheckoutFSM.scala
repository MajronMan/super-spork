package eShop

import java.time.Duration

import akka.actor.{ActorRef, LoggingFSM, Timers}
import eShop.Events.Checkout._
import eShop.Commands.Checkout._
import eShop.Commands.Cart.{CancelCheckout => CartCancelCheckout, CloseCheckout => CartCloseCheckout}
import eShop.Events.Other.Failed
import eShop.States.CheckoutState
import eShop.States.CheckoutState._
import eShop.Data.CheckoutData

object CheckoutFSM {
  val checkoutTimerId = "CheckoutTimerId"
  val paymentTimerId = "PaymentTimerId"
}

class CheckoutFSM(checkoutExpirationTime: Duration = Duration.ofSeconds(1),
                  paymentExpirationTime: Duration = Duration.ofSeconds(1))
  extends LoggingFSM[CheckoutState, CheckoutData] with Timers {

  startWith(Initial, CheckoutData())

  var cartRef: ActorRef = _
  var itemIds: List[String] = _

  when(Initial) {
    case Event(StartCheckout(cartInfo), _) =>
      println("Checkout: Starting checkout with items: " + cartInfo.itemIds)
      this.cartRef = cartInfo.cartRef
      this.itemIds = cartInfo.itemIds
      goto(CheckoutState.SelectingDelivery)
  }

  when(SelectingDelivery) {
    case Event(SelectDeliveryMethod(method), data) =>
      println("Checkout: selected delivery method " + method)
      goto(SelectingPayment) using data.copy(deliveryMethod = Some(method))
  }

  when(SelectingPayment) {
    case Event(SelectPaymentMethod(method), data) =>
      println("Checkout: selected payment method " + method)
      goto(CheckoutState.ProcessingPayment) using data.copy(paymentMethod = Some(method))
  }

  when(ProcessingPayment) {
    case Event(ReceivePayment(), _) =>
      println("Checkout: received payment")
      timers.cancel(CheckoutFSM.paymentTimerId)
      stop(CartCloseCheckout())
      goto(CheckoutState.Closed)
  }

  when(Closed){
    case _ => stay
  }

  whenUnhandled {
    case Event(CheckoutTimerExpired()
               | PaymentTimerExpired(), _) =>
      stop(CartCancelCheckout())
      stay
    case Event(CancelCheckout(), _) =>
      sender ! CheckoutCancelled
      stop(CartCancelCheckout())
      stay
    case unhandled =>
      sender ! Failed()
      println("Checkout: cannot handle " + unhandled)
      context stop self
      stay
  }

  onTransition {
    case Initial -> SelectingDelivery =>
      timers.startSingleTimer(CheckoutFSM.checkoutTimerId, CheckoutTimerExpired(), checkoutExpirationTime)
    case CheckoutState.SelectingPayment -> CheckoutState.ProcessingPayment =>
      timers.startSingleTimer(CheckoutFSM.paymentTimerId, PaymentTimerExpired(), paymentExpirationTime)
      timers.cancel(CheckoutFSM.checkoutTimerId)
  }

  private def stop(cartCommand: Commands.Command): Unit = {
    cartRef ! cartCommand
    context stop self
  }

  initialize()
}
