# AWS::MWAA::Environment LoggingConfiguration

Logging configuration for the environment.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#dagprocessinglogs" title="DagProcessingLogs">DagProcessingLogs</a>" : <i><a href="moduleloggingconfiguration.md">ModuleLoggingConfiguration</a></i>,
    "<a href="#schedulerlogs" title="SchedulerLogs">SchedulerLogs</a>" : <i><a href="moduleloggingconfiguration.md">ModuleLoggingConfiguration</a></i>,
    "<a href="#webserverlogs" title="WebserverLogs">WebserverLogs</a>" : <i><a href="moduleloggingconfiguration.md">ModuleLoggingConfiguration</a></i>,
    "<a href="#workerlogs" title="WorkerLogs">WorkerLogs</a>" : <i><a href="moduleloggingconfiguration.md">ModuleLoggingConfiguration</a></i>,
    "<a href="#tasklogs" title="TaskLogs">TaskLogs</a>" : <i><a href="moduleloggingconfiguration.md">ModuleLoggingConfiguration</a></i>
}
</pre>

### YAML

<pre>
<a href="#dagprocessinglogs" title="DagProcessingLogs">DagProcessingLogs</a>: <i><a href="moduleloggingconfiguration.md">ModuleLoggingConfiguration</a></i>
<a href="#schedulerlogs" title="SchedulerLogs">SchedulerLogs</a>: <i><a href="moduleloggingconfiguration.md">ModuleLoggingConfiguration</a></i>
<a href="#webserverlogs" title="WebserverLogs">WebserverLogs</a>: <i><a href="moduleloggingconfiguration.md">ModuleLoggingConfiguration</a></i>
<a href="#workerlogs" title="WorkerLogs">WorkerLogs</a>: <i><a href="moduleloggingconfiguration.md">ModuleLoggingConfiguration</a></i>
<a href="#tasklogs" title="TaskLogs">TaskLogs</a>: <i><a href="moduleloggingconfiguration.md">ModuleLoggingConfiguration</a></i>
</pre>

## Properties

#### DagProcessingLogs

Logging configuration for a specific airflow component.

_Required_: No

_Type_: <a href="moduleloggingconfiguration.md">ModuleLoggingConfiguration</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### SchedulerLogs

_Required_: No

_Type_: <a href="moduleloggingconfiguration.md">ModuleLoggingConfiguration</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### WebserverLogs

_Required_: No

_Type_: <a href="moduleloggingconfiguration.md">ModuleLoggingConfiguration</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### WorkerLogs

_Required_: No

_Type_: <a href="moduleloggingconfiguration.md">ModuleLoggingConfiguration</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### TaskLogs

_Required_: No

_Type_: <a href="moduleloggingconfiguration.md">ModuleLoggingConfiguration</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

