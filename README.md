# Attivio On-Premises Connectors Integration
The goal of this connector is to ingest Attivio documents from attivio on-premises connectors. The assumption is that the
 on-prem connectors don't have to be modified or reconfigured in order to integrate with Fusion.
 <p>
 The connector  consumes SQS messages and S3 objects from the on-premises connector.  SQS messages are used for control - they point
 to S3 objects that contain the Attivio documents. The documents are ingested and the control messages are deleted.
 <p>
 The on-prem connectors use a single queue for all the control messages that go to the cloud instance. The Attivio cloud instance
 in turn configure and start a connector instance per on-prem instance. This Fusion connector, on the other hand, uses a control
 queue per connector, that means that messages from the single control queue must be forwarded to each  specific connector's queue. AWS
 Lambda will be used for that.
 <p>
 Each instance of this connector is configured with AWS credentials and the name of the on-prem connector. It is secheduled
 to run every few minutes to check the SQS queue. The connector run ends once there are messages on the queue.
 