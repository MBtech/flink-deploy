I would recommend using [my ansible repo](https://github.com/MBtech/ansible-flink) instead of this project for deployment of Flink. Ansible is much more practical and useful. 
Flink-deploy (as the name suggests) is a tool to deploy [Apache Flink](https://github.com/apache/flink) on [Amazon EC2](http://aws.amazon.com/ec2/).

_Please do not hesitate to contact me for potential bugs that you have found along with the features you might want to see in this tool. Your feedback will help to further improve this tool._

This tool is a modification of the Storm deploy tool, adapted to deploy Flink on EC2 instead. The original storm deploy tool can be found [here](https://github.com/KasperMadsen/storm-deploy-alternative). I am also working on potential improving that tool. 

## Features
+ Runs Flink's components (Job Manager, Task Manager and UI client) under supervision (automatically restarted in case of failure)
+ Only fetch and compile what is needed (can deploy on prepared images in a few minutes)
+ Supports executing user-defined commands both pre-config and post-config
+ Automatically sets up s3cmd, making it easy to get/put files on [Amazon S3](http://aws.amazon.com/s3/)
+ Automatically sets up [Amazon EC2 AMI Tools](http://docs.aws.amazon.com/AWSEC2/latest/CommandLineReference/ami-tools.html) on new nodes
+ Supports the latest version of Flink: 0.10.1 along with all available hadoop and scala versions of it

## Configuration
This tool, requires two configuration files: `conf/credential.yaml` and `conf/configuration.yaml`. Put your AWS credentials into the file `conf/credential.yaml`. Two sample files are provided for each of the configuration files that you can use as guideline. 

Below is an example of a single cluster configuration, for `conf/configuration.yaml`

```
mycluster:
    - m1.medium {WORKER, MASTER, UI}
    - m1.medium {WORKER}
    - flink-version "0.10.1"					
    - hadoop-version "2.6.0"					
    - scala-version "2.10"                   
    - image "eu-west-1/ami-97344ae0" 	#official Ubuntu 14.04 LTS AMI
    - region "eu-west-1"
    - remote-exec-preconfig {cd ~, echo hey > hey.txt}
    - remote-exec-postconfig {}
```
+ MASTER is the node where Job Manager runs
+ WORKER is the node with Task Manager
+ UI is the Flink's web interface 
+ Option for scala version specification is only available for Flink version 0.10.1
+ Currently supported Flink versions are: 0.10.1, 0.9.1 and 0.9.0 (I have tested it with 0.10.1, if you find any problem with other versions i.e. 0.9.1 and 0.9.0 let me know. I will test them soon, I hope)

_Please ensure the image resides in the same region as specified._

## Usage

### Deploy
Execute `java -jar flink-deploy-1.jar deploy CLUSTER_NAME`

The `CLUSTER_NAME` is what you specify for your cluster. For example, in the above example "mycluster" is the CLUSTER_NAME
After successful deployment, the IP addresses of the nodes with specified roles are printed out on the terminal. 

### Kill
Execute `java -jar flink-deploy-1.jar kill CLUSTER_NAME`

Kills all nodes belonging in the cluster with name CLUSTER_NAME.

## FAQ
+ I am seeing the error: `net.schmizz.sshj.userauth.UserAuthException: publickey auth failed`. This error means the software could not connect to the newly launched instances using SSH (for configuring them). There can be multiple reasons why this error happens. Please ensure you have ~/.ssh/id_rsa and ~/.ssh/id_rsa.pub and that both files are _valid_. Furthermore, please go to AWS EC2 interface -> Key Pairs, and delete the jclouds#CLUSTER_NAME keypair. If deploying the same cluster, using multiple machines, please ensure the same keypair exists on all machines. In case problems persist, please try generating a new keypair by executing `ssh-keygen -t rsa`, then delete old keypair from AWS EC2 interface and retry deployment.
+ I am seeing the warning: `cipher strengths apparently limited by JCE policy`. You can improve your security by installing [Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy Files](http://www.oracle.com/technetwork/java/javase/downloads/index.html).
+ I am seeing the error: `the trustAnchors parameter must be non-empty`. This error usually means the Java CA certificates are broken. To fix first execute `sudo dpkg --purge --force-depends ca-certificates-java` then `sudo apt-get install ca-certificates-java`.

## Limitations
Currently, only deployment to Ubuntu AMIs on Amazon EC2 is supported.

## Experimental
+ Right now HDFS storage layer is experimental and I am fiddling with configuration. HDFS is downloaded and configured but it is not started automatically. You might want to modify the configuration a little. The default configurations might not work since it is still a work in progress. 

## Work-in-progress
+ Configuration of HDFS storage for Flink
+ Support for more versions of Flink

## Future Work
+ Deployment on other cloud providers
+ Deployment on other Linux flavors
+ Support for integrate Kafka deployment
