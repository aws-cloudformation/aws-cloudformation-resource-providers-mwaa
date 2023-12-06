# AWS::MWAA::Environment

Resource schema for AWS::MWAA::Environment

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "Type" : "AWS::MWAA::Environment",
    "Properties" : {
        "<a href="#name" title="Name">Name</a>" : <i>String</i>,
        "<a href="#executionrolearn" title="ExecutionRoleArn">ExecutionRoleArn</a>" : <i>String</i>,
        "<a href="#kmskey" title="KmsKey">KmsKey</a>" : <i>String</i>,
        "<a href="#airflowversion" title="AirflowVersion">AirflowVersion</a>" : <i>String</i>,
        "<a href="#sourcebucketarn" title="SourceBucketArn">SourceBucketArn</a>" : <i>String</i>,
        "<a href="#dags3path" title="DagS3Path">DagS3Path</a>" : <i>String</i>,
        "<a href="#pluginss3path" title="PluginsS3Path">PluginsS3Path</a>" : <i>String</i>,
        "<a href="#pluginss3objectversion" title="PluginsS3ObjectVersion">PluginsS3ObjectVersion</a>" : <i>String</i>,
        "<a href="#requirementss3path" title="RequirementsS3Path">RequirementsS3Path</a>" : <i>String</i>,
        "<a href="#requirementss3objectversion" title="RequirementsS3ObjectVersion">RequirementsS3ObjectVersion</a>" : <i>String</i>,
        "<a href="#startupscripts3path" title="StartupScriptS3Path">StartupScriptS3Path</a>" : <i>String</i>,
        "<a href="#startupscripts3objectversion" title="StartupScriptS3ObjectVersion">StartupScriptS3ObjectVersion</a>" : <i>String</i>,
        "<a href="#airflowconfigurationoptions" title="AirflowConfigurationOptions">AirflowConfigurationOptions</a>" : <i>Map</i>,
        "<a href="#environmentclass" title="EnvironmentClass">EnvironmentClass</a>" : <i>String</i>,
        "<a href="#maxworkers" title="MaxWorkers">MaxWorkers</a>" : <i>Integer</i>,
        "<a href="#minworkers" title="MinWorkers">MinWorkers</a>" : <i>Integer</i>,
        "<a href="#schedulers" title="Schedulers">Schedulers</a>" : <i>Integer</i>,
        "<a href="#networkconfiguration" title="NetworkConfiguration">NetworkConfiguration</a>" : <i><a href="networkconfiguration.md">NetworkConfiguration</a></i>,
        "<a href="#loggingconfiguration" title="LoggingConfiguration">LoggingConfiguration</a>" : <i><a href="loggingconfiguration.md">LoggingConfiguration</a></i>,
        "<a href="#weeklymaintenancewindowstart" title="WeeklyMaintenanceWindowStart">WeeklyMaintenanceWindowStart</a>" : <i>String</i>,
        "<a href="#tags" title="Tags">Tags</a>" : <i>Map</i>,
        "<a href="#webserveraccessmode" title="WebserverAccessMode">WebserverAccessMode</a>" : <i>String</i>,
        "<a href="#endpointmanagement" title="EndpointManagement">EndpointManagement</a>" : <i>String</i>,
    }
}
</pre>

### YAML

<pre>
Type: AWS::MWAA::Environment
Properties:
    <a href="#name" title="Name">Name</a>: <i>String</i>
    <a href="#executionrolearn" title="ExecutionRoleArn">ExecutionRoleArn</a>: <i>String</i>
    <a href="#kmskey" title="KmsKey">KmsKey</a>: <i>String</i>
    <a href="#airflowversion" title="AirflowVersion">AirflowVersion</a>: <i>String</i>
    <a href="#sourcebucketarn" title="SourceBucketArn">SourceBucketArn</a>: <i>String</i>
    <a href="#dags3path" title="DagS3Path">DagS3Path</a>: <i>String</i>
    <a href="#pluginss3path" title="PluginsS3Path">PluginsS3Path</a>: <i>String</i>
    <a href="#pluginss3objectversion" title="PluginsS3ObjectVersion">PluginsS3ObjectVersion</a>: <i>String</i>
    <a href="#requirementss3path" title="RequirementsS3Path">RequirementsS3Path</a>: <i>String</i>
    <a href="#requirementss3objectversion" title="RequirementsS3ObjectVersion">RequirementsS3ObjectVersion</a>: <i>String</i>
    <a href="#startupscripts3path" title="StartupScriptS3Path">StartupScriptS3Path</a>: <i>String</i>
    <a href="#startupscripts3objectversion" title="StartupScriptS3ObjectVersion">StartupScriptS3ObjectVersion</a>: <i>String</i>
    <a href="#airflowconfigurationoptions" title="AirflowConfigurationOptions">AirflowConfigurationOptions</a>: <i>Map</i>
    <a href="#environmentclass" title="EnvironmentClass">EnvironmentClass</a>: <i>String</i>
    <a href="#maxworkers" title="MaxWorkers">MaxWorkers</a>: <i>Integer</i>
    <a href="#minworkers" title="MinWorkers">MinWorkers</a>: <i>Integer</i>
    <a href="#schedulers" title="Schedulers">Schedulers</a>: <i>Integer</i>
    <a href="#networkconfiguration" title="NetworkConfiguration">NetworkConfiguration</a>: <i><a href="networkconfiguration.md">NetworkConfiguration</a></i>
    <a href="#loggingconfiguration" title="LoggingConfiguration">LoggingConfiguration</a>: <i><a href="loggingconfiguration.md">LoggingConfiguration</a></i>
    <a href="#weeklymaintenancewindowstart" title="WeeklyMaintenanceWindowStart">WeeklyMaintenanceWindowStart</a>: <i>String</i>
    <a href="#tags" title="Tags">Tags</a>: <i>Map</i>
    <a href="#webserveraccessmode" title="WebserverAccessMode">WebserverAccessMode</a>: <i>String</i>
    <a href="#endpointmanagement" title="EndpointManagement">EndpointManagement</a>: <i>String</i>
</pre>

## Properties

#### Name

Customer-defined identifier for the environment, unique per customer region.

_Required_: Yes

_Type_: String

_Minimum Length_: <code>1</code>

_Maximum Length_: <code>80</code>

_Pattern_: <code>^[a-zA-Z][0-9a-zA-Z\-_]*$</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### ExecutionRoleArn

IAM role to be used by tasks.

_Required_: No

_Type_: String

_Maximum Length_: <code>1224</code>

_Pattern_: <code>^arn:(aws|aws-us-gov|aws-cn|aws-iso|aws-iso-b)(-[a-z]+)?:iam::\d{12}:role/?[a-zA-Z_0-9+=,.@\-_/]+$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### KmsKey

The identifier of the AWS Key Management Service (AWS KMS) customer master key (CMK) to use for MWAA data encryption.

    You can specify the CMK using any of the following:

    Key ID. For example, key/1234abcd-12ab-34cd-56ef-1234567890ab.

    Key alias. For example, alias/ExampleAlias.

    Key ARN. For example, arn:aws:kms:us-east-1:012345678910:key/abcd1234-a123-456a-a12b-a123b4cd56ef.

    Alias ARN. For example, arn:aws:kms:us-east-1:012345678910:alias/ExampleAlias.

    AWS authenticates the CMK asynchronously. Therefore, if you specify an ID, alias, or ARN that is not valid, the action can appear to complete, but eventually fails.

_Required_: No

_Type_: String

_Maximum Length_: <code>1224</code>

_Pattern_: <code>^(((arn:(aws|aws-us-gov|aws-cn|aws-iso|aws-iso-b)(-[a-z]+)?:kms:[a-z]{2}-[a-z]+-\d:\d+:)?key\/)?[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}|(arn:(aws|aws-us-gov|aws-cn|aws-iso|aws-iso-b):kms:[a-z]{2}-[a-z]+-\d:\d+:)?alias/.+)$</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### AirflowVersion

Version of airflow to deploy to the environment.

_Required_: No

_Type_: String

_Maximum Length_: <code>32</code>

_Pattern_: <code>^[0-9a-z.]+$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### SourceBucketArn

ARN for the AWS S3 bucket to use as the source of DAGs and plugins for the environment.

_Required_: No

_Type_: String

_Minimum Length_: <code>1</code>

_Maximum Length_: <code>1224</code>

_Pattern_: <code>^arn:(aws|aws-us-gov|aws-cn|aws-iso|aws-iso-b)(-[a-z]+)?:s3:::[a-z0-9.\-]+$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### DagS3Path

Represents an S3 prefix relative to the root of an S3 bucket.

_Required_: No

_Type_: String

_Maximum Length_: <code>1024</code>

_Pattern_: <code>.*</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### PluginsS3Path

Represents an S3 prefix relative to the root of an S3 bucket.

_Required_: No

_Type_: String

_Maximum Length_: <code>1024</code>

_Pattern_: <code>.*</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### PluginsS3ObjectVersion

Represents an version ID for an S3 object.

_Required_: No

_Type_: String

_Maximum Length_: <code>1024</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### RequirementsS3Path

Represents an S3 prefix relative to the root of an S3 bucket.

_Required_: No

_Type_: String

_Maximum Length_: <code>1024</code>

_Pattern_: <code>.*</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### RequirementsS3ObjectVersion

Represents an version ID for an S3 object.

_Required_: No

_Type_: String

_Maximum Length_: <code>1024</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### StartupScriptS3Path

Represents an S3 prefix relative to the root of an S3 bucket.

_Required_: No

_Type_: String

_Maximum Length_: <code>1024</code>

_Pattern_: <code>.*</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### StartupScriptS3ObjectVersion

Represents an version ID for an S3 object.

_Required_: No

_Type_: String

_Maximum Length_: <code>1024</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### AirflowConfigurationOptions

Key/value pairs representing Airflow configuration variables.
    Keys are prefixed by their section:

    [core]
    dags_folder={AIRFLOW_HOME}/dags

    Would be represented as

    "core.dags_folder": "{AIRFLOW_HOME}/dags"

_Required_: No

_Type_: Map

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### EnvironmentClass

Templated configuration for airflow processes and backing infrastructure.

_Required_: No

_Type_: String

_Minimum Length_: <code>1</code>

_Maximum Length_: <code>1024</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### MaxWorkers

Maximum worker compute units.

_Required_: No

_Type_: Integer

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### MinWorkers

Minimum worker compute units.

_Required_: No

_Type_: Integer

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Schedulers

Scheduler compute units.

_Required_: No

_Type_: Integer

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### NetworkConfiguration

Configures the network resources of the environment.

_Required_: No

_Type_: <a href="networkconfiguration.md">NetworkConfiguration</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### LoggingConfiguration

Logging configuration for the environment.

_Required_: No

_Type_: <a href="loggingconfiguration.md">LoggingConfiguration</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### WeeklyMaintenanceWindowStart

Start time for the weekly maintenance window.

_Required_: No

_Type_: String

_Maximum Length_: <code>9</code>

_Pattern_: <code>(MON|TUE|WED|THU|FRI|SAT|SUN):([01]\d|2[0-3]):(00|30)</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Tags

A map of tags for the environment.

_Required_: No

_Type_: Map

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### WebserverAccessMode

Choice for mode of webserver access including over public internet or via private VPC endpoint.

_Required_: No

_Type_: String

_Allowed Values_: <code>PRIVATE_ONLY</code> | <code>PUBLIC_ONLY</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### EndpointManagement

Defines whether the VPC endpoints configured for the environment are created, and managed, by the customer or by Amazon MWAA.

_Required_: No

_Type_: String

_Allowed Values_: <code>CUSTOMER</code> | <code>SERVICE</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

## Return Values

### Ref

When you pass the logical ID of this resource to the intrinsic `Ref` function, Ref returns the Name.

### Fn::GetAtt

The `Fn::GetAtt` intrinsic function returns a value for a specified attribute of this type. The following are the available attributes and sample return values.

For more information about using the `Fn::GetAtt` intrinsic function, see [Fn::GetAtt](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-getatt.html).

#### Arn

ARN for the MWAA environment.

#### CeleryExecutorQueue

The celery executor queue associated with the environment.

#### DatabaseVpcEndpointService

The database VPC endpoint service name.

#### WebserverVpcEndpointService

The webserver VPC endpoint service name, applicable if private webserver access mode selected.

#### WebserverUrl

Url endpoint for the environment's Airflow UI.

#### CloudWatchLogGroupArn

Returns the <code>CloudWatchLogGroupArn</code> value.

#### CloudWatchLogGroupArn

Returns the <code>CloudWatchLogGroupArn</code> value.

#### CloudWatchLogGroupArn

Returns the <code>CloudWatchLogGroupArn</code> value.

#### CloudWatchLogGroupArn

Returns the <code>CloudWatchLogGroupArn</code> value.

#### CloudWatchLogGroupArn

Returns the <code>CloudWatchLogGroupArn</code> value.

