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
package net.schmizz.sshj.xfer;

import java.io.File;
import java.io.IOException;


/**
 * Default implementation of {@link ModeSetter} attempts to preserve timestamps and permissions to the extent allowed by
 * Java File API.
 */
public class DefaultModeSetter
        implements ModeSetter {

    @Override
    public void setLastAccessedTime(File f, long t)
            throws IOException {
        // Can't do anything
    }

    @Override
    public void setLastModifiedTime(File f, long t)
            throws IOException {
        f.setLastModified(t * 1000);
    }

    @Override
    public void setPermissions(File f, int perms)
            throws IOException {
        f.setReadable(FilePermission.USR_R.isIn(perms),
                      !(FilePermission.OTH_R.isIn(perms) || FilePermission.GRP_R.isIn(perms)));
        f.setWritable(FilePermission.USR_W.isIn(perms),
                      !(FilePermission.OTH_W.isIn(perms) || FilePermission.GRP_W.isIn(perms)));
        f.setExecutable(FilePermission.USR_X.isIn(perms),
                        !(FilePermission.OTH_X.isIn(perms) || FilePermission.GRP_X.isIn(perms)));
    }

    @Override
    public boolean preservesTimes() {
        return true;
    }

}