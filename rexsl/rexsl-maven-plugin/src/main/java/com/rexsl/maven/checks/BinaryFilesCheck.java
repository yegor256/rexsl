/**
 * Copyright (c) 2011-2012, ReXSL.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the ReXSL.com nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.rexsl.maven.checks;

import com.rexsl.maven.Check;
import com.rexsl.maven.Environment;
import com.ymock.util.Logger;
import java.io.File;
import java.util.Collection;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.AndFileFilter;
import org.apache.commons.io.filefilter.HiddenFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.NotFileFilter;

/**
 * Checks that all files are text ones.
 *
 * <p>You shouldn't keep any binary files (images, video, etc.) in
 * {@code src/main/webapp} folder and deploy them inside your {@code WAR}
 * package. Instead, you should use some storage service
 * (<a href="http://aws.amazon.com/s3">Amazon S3</a>, for example) and a
 * content delivery network (CDN) on top of it
 * (<a href="http://aws.amazon.com/cloudfront">Amazon CloudFront</a>,
 * for example).
 *
 * <p>This check validates all files found in {@code src/main/webapp} and
 * fails your build if any binary files are found there.
 *
 * @author Dmitry Bashkin (dmitry.bashkin@rexsl.com)
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id: BinaryFilesCheck.java 204 2011-10-26 21:15:28Z guard $
 */
final class BinaryFilesCheck implements Check {

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean validate(final Environment env) {
        final File dir = new File(env.basedir(), "src/main/webapp");
        final Collection<File> files = FileUtils.listFiles(
            dir,
            HiddenFileFilter.VISIBLE,
            new AndFileFilter(
                HiddenFileFilter.VISIBLE,
                new NotFileFilter(new NameFileFilter(".svn"))
            )
        );
        boolean valid = true;
        for (File file : files) {
            final String path = file.getAbsolutePath()
                .substring(dir.getAbsolutePath().length() + 1);
            final String ext = FilenameUtils.getExtension(path);
            if (!ext.matches("html|xml|xhtml|txt|xsl|css|js")) {
                Logger.warn(
                    this,
                    "File %s has incorrect type/extension '%s'",
                    file,
                    ext
                );
                valid = false;
                break;
            }
        }
        return valid;
    }
}
