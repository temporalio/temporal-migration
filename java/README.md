# temporal-migration :: Java

## Applications

1. Start temporal local dev server
2. Start `LegacyApplication` (cfg: [application-legacy.yml](examples/backend/src/main/resources/application-legacy.yaml))
   1. This represents the application already running in your self-hosted environment
3. Start `TargetApplication` (cfg: [application-target.yml](examples/backend/src/main/resources/application-target.yaml))
   1. This represents the application running in your Temporal Cloud environment
4. Start `SimulatorApplication` (cfg: [application-simulation.yml](examples/frontend/src/main/resources/application-simulation.yaml))
   1. This is the Simulator you are running to cross-check signals against what you see in your target Namespace.

## Starters

1. Run `SeedStarterApplication` (cfg: [application-starter.yml](examples/frontend/src/main/resources/application-starter.yaml))
   1. Cleans both the target and legacy environments (BE CAREFUL)
      1. Fails occasionally due to race with batch jobs in local dev. Just re-run it
   2. Starts `${spring.simulation.execution-count}` executions in your legacy application
   3. Starts simulator to send signals with timestamp every `${spring.simulation.signal-frequency-millis}` milliseconds to legacy application, failing over to the target when migrated
      1. Sends only `${spring.simulation.signal-target-threshold-count}` signals to the target after migration to allow exit of simulator
2. Run `MigrateStarterApplication` (cfg: [application-starter.yml](examples/frontend/src/main/resources/application-starter.yaml))
   1. Sends the `migrateIt` batch signal into your Legacy Application to
      1. Cancel the current execution
      2. Start an execution in the Target Namespace with the current `getMigrationState` query result
      3. Exit the execution with `Completed` status

### Migration.Support Package

The [support](migration/src/main/java/io/temporal/migration/support) package has reusable facilities for pushing and pulling 
`getMigrationState` query results between your Legacy and Target Namespaces.

The `pullLegacyExecutionInfo` Activity polls the corresponding legacy execution to pull `Completed` workflow execution state over to the Target execution.

