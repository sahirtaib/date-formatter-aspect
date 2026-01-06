# Overview

This project provides an [AspectJ](https://www.eclipse.org/aspectj/) aspect that intercepts calls to/overrides `org.joget.plugin.enterprise.DateFormatter.format()` method to log more verbose info when exception is thrown.

Additional info logged is:

- App Id
- App Version
- DataList Id
- row data

Sample info logged in Monitor > System Logs:
```
DateFormatterAspect - appId=myApp, appVersion=1, dataList.id=list_of_records, {field1=test, id=2156c0cb-f23c-4591-828f-5c2bf24d62da, field3=2026/01/02, field2=test}, dataFormat=yyyy-MM-dd, displayFormat=MM/dd/yyyy
```

This info is useful to identify which database data is causing below exception pattern:
```
java.text.ParseException: Unparseable date: "2026/01/02"
```

## How to Use

1. Build the jar file with `mvn clean install`.
2. Copy the `target/date-formatter-aspect-{version}.jar` file into `tomcat/webapps/jw/WEB-INF/lib`.
3. Restart Joget/Tomcat

## License

This software is provided as-is without warranty. No liability is assumed for any damages resulting from the use of this code.