# Doctor Batch's Magic Sauce
## About
Doctor Batch's Magic Sauce is provides two key capabilities to batch application developers adopting IBM's Java Batch platforms built on WebSphere Application Server:
- An application framework for development of batch applications in the Java programming language for deployment and parallel testing in WebSphere Batch ("traditional" WebSphere Application Server) and Java EE Batch (Liberty Profile v9 Feature with EE 7)
- A library of reusable batch artifacts (readers, writers, processors, listeners, etc) for which there are other IBM dependencies - for example:
	- Readers and Writers using IBM JZOS to access record oriented data in MVS
	- Processor implementations that invoke IBM Operational Decision Manager business rules applications

## Building from Source
To build from source, you must import several IBM product libraries as third-party JARs into your Maven repository. These include:
- IBM JZOS: jzos-2.4.jar, marshall-1.0.jar
- IBM WebSphere's Batch Runtime: com.ibm.ws.batch.runtime-8.5.5.jar
- IBM ODM's Libraries: jrules-engine.jar, jrules-res-execution.jar, jrules-res-session-java.jar, jrules-res-session-ejb3-WAS85.jar

Instructions on populating your dependency repository are coming soon.

## Downloading the Magic Sauce Library 
The current release is available for download under the "dist/" folder

## Getting Started
Documentation to get started quickly, along with a sample application, are coming very soon!

## Help!
Please send an e-mail to [drbatch@us.ibm.com](mailto:drbatch@us.ibm.com).

http://doc.torbat.ch

