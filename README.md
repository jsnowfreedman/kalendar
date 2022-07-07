# Getting started from a new clone

- Have a postgres instance running on the correct ports
    - Option 1: Run the docker-compose in the docker/ folder.
    - Option 2: Run your own postgres container.
    - Option 3: Run/connect to a postgres server wherever.
- If a new database instance, run the sql statements in `db/migrations` in order - you may also want to run the sql statements in `db/example` in order as well
- If old database instance, make sure you run the latest migrations as needed. (We should think/find a better way of handling this at some point)
- Run the gradle task `generateExposedCode` (In IDEs, its found under the gradle Build tasks folder)
- Create/get an oauth json file to throw into the secrets/oauth folder. If you know what you are doing you can create
  your own with your own SSO system - otherwise, if you are a member of DMS and wanting to help, reach out to Joshua on
  the infra team. 