Replay Test Matrix
Purpose

This document defines the replay/runtime validation scenarios for the Hivra/ShopMe sync runtime.

The goal is to ensure:

replay orchestration correctness
replay lifecycle consistency
reconnect stability
offline-first behavior
worker coordination correctness
replay protection semantics

This matrix is used for:

regression testing
runtime validation
replay debugging
architecture refactoring safety
production hardening
Runtime Replay Architecture

Current replay pipeline:

Runtime Event
-> SyncRuntimeOrchestrator
-> SyncScheduler
-> SyncWorker
-> SyncCoordinator

Replay flow is fully runtime-owned.

Replay States

Current replay execution states:

IDLE
RUNNING
Replay Reasons

Current replay reasons:

STARTUP
RECONNECT
MANUAL
Validation Scenarios
1. Startup Replay
   Goal

Validate startup replay orchestration.

Steps
Force stop app
Start app with network enabled
Expected Logs
Startup replay triggered
Replay requested
Replay execution state -> RUNNING
Replay enqueued
Worker started
Sync success
Replay completed
Replay execution state -> IDLE
Expected Behavior
exactly one startup replay
no duplicate workers
replay lifecycle closes correctly
2. Startup Replay Suppression
   Goal

Validate reconnect suppression after startup replay.

Steps
Force stop app
Start app
Wait for initial network reconnect callback
Expected Logs
Reconnect detected
Reconnect replay suppressed | startup replay active
Expected Behavior
no reconnect replay after startup replay
no second worker execution
3. Reconnect Replay
   Goal

Validate reconnect-triggered replay.

Steps
Start app
Disable network
Re-enable network
Expected Logs
connected=false
connected=true
Reconnect detected
Replay requested | reason=RECONNECT
Replay execution state -> RUNNING
Replay enqueued
Worker started
Sync success
Replay completed
Replay execution state -> IDLE
Expected Behavior
reconnect replay executes successfully
replay lifecycle remains consistent
4. Replay Cooldown Protection
   Goal

Validate replay cooldown suppression.

Steps
Trigger reconnect replay
Trigger another reconnect within cooldown window
Expected Logs
Reconnect replay suppressed | cooldown active
Expected Behavior
no additional replay execution
no additional worker creation
5. Replay Overlap Protection
   Goal

Validate single-active replay semantics.

Steps
Trigger replay
Trigger second replay while replay state is RUNNING
Expected Logs
Replay suppressed | replay already running
Expected Behavior
no overlapping replay execution
no duplicate workers
replay state remains consistent
6. Offline Startup
   Goal

Validate offline-first startup behavior.

Steps
Force stop app
Disable network
Start app
Expected Logs
Runtime started
Startup replay triggered
Replay enqueued
Expected Behavior
app starts successfully offline
local Room state loads
no infinite loading state
worker waits for network constraints
7. Replay Lifecycle Completion
   Goal

Validate replay lifecycle closure.

Steps
Trigger replay
Wait for worker completion
Expected Logs
Replay execution state -> RUNNING
Replay completed
Replay execution state -> IDLE
Expected Behavior
replay state always returns to IDLE
no stuck RUNNING state
Known Protection Layers

Current replay protection mechanisms:

startup replay suppression
replay cooldown
replay overlap protection
unique work scheduling
network constraints
Future Runtime Work

Planned future runtime phases:

persisted replay recovery
process death recovery
replay persistence
replay retry orchestration
replay state persistence
advanced recovery coordination
replay telemetry