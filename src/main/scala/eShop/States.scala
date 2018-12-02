package eShop

import akka.persistence.fsm.PersistentFSM.FSMState

object States {

  sealed trait CartState extends FSMState
  sealed trait CheckoutState extends FSMState
  sealed trait OrderManagerState extends FSMState

  object CartState {

    case object Empty extends CartState {
      override def identifier: String = "cartEmpty"
    }

    case object NonEmpty extends CartState {
      override def identifier: String = "cartNonEmpty"
    }

    case object InCheckout extends CartState {
      override def identifier: String = "cartInCheckout"
    }

  }

  object CheckoutState {
    case object Initial extends CheckoutState {
      override def identifier: String = "Initial"
    }

    case object SelectingDelivery extends CheckoutState {
      override def identifier: String = "SelectingDelivery"
    }

    case object SelectingPayment extends CheckoutState {
      override def identifier: String = "SelectingPayment"
    }

    case object ProcessingPayment extends CheckoutState {
      override def identifier: String = "ProcessingPayment"
    }

    case object Closed extends CheckoutState {
      override def identifier: String = "Closed"
    }
  }

  object OrderManagerState  {
    case object Uninitialized extends OrderManagerState{
      override def identifier: String = "OMUninitialized"
    }
    case object Open extends OrderManagerState{
      override def identifier: String = "OMOpen"
    }
    case object InCheckout extends OrderManagerState{
      override def identifier: String = "OMInCheckout"
    }
    case object InPayment extends OrderManagerState{
      override def identifier: String = "OMInPayment"
    }
    case object Finished extends OrderManagerState{
      override def identifier: String = "OMFinished"
    }
  }

}
