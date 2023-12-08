# AWS::MWAA::Environment ModuleLoggingConfiguration

Logging configuration for a specific airflow component.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#enabled" title="Enabled">Enabled</a>" : <i>Boolean</i>,
    "<a href="#loglevel" title="LogLevel">LogLevel</a>" : <i>String</i>,
    "<a href="#cloudwatchloggrouparn" title="CloudWatchLogGroupArn">CloudWatchLogGroupArn</a>" : <i>String</i>
}
</pre>

### YAML

<pre>
<a href="#enabled" title="Enabled">Enabled</a>: <i>Boolean</i>
<a href="#loglevel" title="LogLevel">LogLevel</a>: <i>String</i>
<a href="#cloudwatchloggrouparn" title="CloudWatchLogGroupArn">CloudWatchLogGroupArn</a>: <i>String</i>
</pre>

## Properties

#### Enabled

_Required_: No

_Type_: Boolean

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### LogLevel

_Required_: No

_Type_: String

_Allowed Values_: <code>CRITICAL</code> | <code>ERROR</code> | <code>WARNING</code> | <code>INFO</code> | <code>DEBUG</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### CloudWatchLogGroupArn

_Required_: No

_Type_: String

_Maximum Length_: <code>1224</code>

_Pattern_: <code>^arn:(aws|aws-us-gov|aws-cn|aws-iso|aws-iso-b)(-[a-z]+)?:logs:[a-z0-9\-]+:\d{12}:log-group:\w+</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

