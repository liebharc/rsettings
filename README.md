A sandbox area to try out a few code patterns for settings, not intended to be really used at some point. If someone looks for a good library for reactive settings there is this promising project: https://github.com/cpdevoto/reactive-properties

The implementation is based on an immutable state where each update to the state gives a new state. Updates to a state are done with a builder pattern which allows to combine several changes
into one. Consistency of the state therefore only needs to be checked every time a builder builds a new state. If a state is inconsistent then the builder can just throw an exception. 
Because the previous version of the state will always still exist (because it has never been changed) rollbacks don't need to be implemented. In the end changes to the state appear to be atomic.

On top of the immutable implementation there is also a mutable wrapper. This mutable state adds some additional convenience methods, some events and a thread safe access
to the immutable state.