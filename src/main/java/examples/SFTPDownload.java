/*
 * Copyright 2010 Shikhar Bhushan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package examples;

import net.schmizz.sshj.SSHClient;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.PatternLayout;

/** This example demonstrates downloading of a file over SFTP from the SSH server. */
public class SFTPDownload {

    static {
        BasicConfigurator.configure(new ConsoleAppender(new PatternLayout("%d [%-15.15t] %-5p %-30.30c{1} - %m%n")));
    }

    public static void main(String[] args) throws Exception {
        SSHClient ssh = new SSHClient();
        ssh.loadKnownHosts();
        ssh.connect("localhost");
        try {
            ssh.authPublickey(System.getProperty("user.name"));
            ssh.newSFTPClient().get("well", "/tmp/");
        } finally {
            ssh.disconnect();
        }
    }

}