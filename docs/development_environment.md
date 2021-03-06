# Biobank development environment

Follow these instructions to setup a Linux computer to be used for building or developing software
for Biobank.

## Install required software

1.  Using the Ubuntu package manager install the required packages.

    ```bash
	sudo apt-get install ant git-core zip unzip
	```

1.  Follow these [instructions](ubuntu_java_install.md) to install the Java Standard Edition 6
    Development Kit.

1.  Download **JBoss 4.0.5** from
    [http://www.jboss.org/jbossas/downloads/](http://www.jboss.org/jbossas/downloads/).

    For security reasons, it is better to run JBoss as a non root user and take other precautions
    given here: [JBoss](jboss_configuration.md). To do this, using a **root** account, create a
    jboss user account:

    ```bash
    useradd --system -d /opt/jboss -s /bin/bash jboss
	````

    and unzip the downloaded file to the jboss acount home directory (JBoss will then be in
    `/opt/jboss/jboss-4.0.5.GA`).

1.  Download **Eclipse for RCP and RAP Developers** from this
    [page](http://www.eclipse.org/downloads/packages/release/indigo/sr2).  Make sure you download the
    64-bit version if you are running 64-bit Ubuntu. Extract the file to your **Eclipse Home**
    directory. For example, if my **Eclipse Home** directory is `/opt/eclipse`, then use the
    following commands (this must be done using the **root** account):

    ```bash
	mkdir /opt/eclipse
	cd /opt/eclipse
	mv eclipse-rcp-indigo-SR2-linux-gtk-x86_64.tar.gz .
	tar zxvf eclipse-rcp-indigo-SR2-linux-gtk-x86_64.tar.gz
	```

    This results in having the directory `/opt/eclipse/eclipse`.

1.  Download the **Eclipse Delta Pack** from this
    [page](http://archive.eclipse.org/eclipse/downloads/drops/R-3.7.2-201202080800/#DeltaPack) and
    copy the file to your `eclipse` subdirectory in your **Eclipse Home** directory. From that
    directory extract the files  (this must be done using the **root** account). For example:

    ```bash
	cp eclipse-3.7.2-delta-pack.zip /opt/eclipse/eclipse
	cd /opt/eclipse/eclipse
	unzip eclipse-3.7.2-delta-pack.zip
	```

    The delta pack is required to build the client for the other operating systems. The delta pack
    version must match the version of Eclipse being used.

1.  Create a bash profile file to define enviroment variables. Create the file
    `/opt/jboss/.bash_profile` and add the following lines:

    ```
	export JBOSS_HOME=/opt/jboss/jboss-4.0.5.GA
    export ECLIPSE_HOME=/opt/eclipse/eclipse
	```

1.  Retrieve the Biobank source code from the GitHub repository to your **Biobank Source Code Home**
    directory. It is recommended to use `/opt/jboss/biobank`. Use the following commands starting
    with a shell logged into the root account.

    ```bash
	su - jboss
	cd /opt/jboss
	git clone https://github.com/cbsrbiobank/biobank.git
	cd biobank
	git checkout TAG
	git submodule update
	```

    Replace `TAG` with the tag name for the version you want to build. To see a list of all tags use
    the command:

    ```bash
	git tag
	```


****

[Back to parent document](client_build.md)

[Back to top](../README.md)
