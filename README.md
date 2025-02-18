# Setup
1. Update `application.properties`
   
> Application name
> ```properties
> spring.application.name=<your app name>
> ```
> Database connection
> ```properties
> spring.datasource.url=jdbc:mysql://<ip>:<port>/<default_schema>?autoReconnect=true&useUnicode=true&characterEncoding=utf-8&zeroDateTimeBehavior=round&useSSL=true
> spring.datasource.username=
> spring.datasource.password=
> ```
> Auto create/update base tables - On initial run, keep this `true`. After base tables are created you can set this to `false`.
> ```properties
> template.app.autoUpdateTables=true
> ```
