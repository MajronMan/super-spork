package eShop

import akka.actor.ActorRef

object Events {
  sealed trait Event
  object Checkout {

    case class CheckoutStarted() extends Event

    case class DeliveryMethodSelected(id: String) extends Event

    case class PaymentSelected(id: String) extends Event

    case class PaymentReceived() extends Event

    case class CheckoutCancelled() extends Event

    case class CheckoutClosed() extends Event

    case class CheckoutTimerExpired()

    case class PaymentTimerExpired()
  }

  object Cart {
    case class ItemAdded(id: String) extends Event

    case class ItemRemoved(id: String) extends Event

    case class CheckoutStarted() extends Event

    case class CheckoutCancelled() extends Event

    case class CheckoutClosed() extends Event

    case class TimerExpired() extends Event
  }

  object OrderManager {
    case class CheckoutStarted(checkoutRef: ActorRef) extends Event
    case object CartEmptied extends Event
    case class PaymentServiceStarted(paymentRef: ActorRef) extends Event
    case object PaymentConfirmed extends Event
    case object CheckoutCancelled extends Event
  }

  object Other {
    case class Failed() extends Event
  }
}
