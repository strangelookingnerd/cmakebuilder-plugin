package hudson.plugins.cmake;

import org.kohsuke.stapler.DataBoundSetter;

import hudson.CopyOnWrite;
import hudson.FilePath;
import hudson.Util;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;

/**
 * A Builder that holds information about a cmake installation, the working
 * directory for invocation and the arguments to pass to {@code cmake},
 * 
 * @author Martin weber
 *
 */
public abstract class AbstractCmakeBuilder extends Builder {

    /** the name of the cmake tool installation to use for this build step */
    private String installationName;
    private String workingDir;
    private String cmakeArgs;

    /**
     * Minimal constructor.
     *
     * @param installationName
     *            the name of the cmake tool installation from the global config
     *            page.
     * @param generator
     *            the name of cmake´s buildscript generator. May be empty but
     *            not {@code null}
     */
    public AbstractCmakeBuilder(String installationName) {
        this.installationName = Util.fixEmptyAndTrim(installationName);
    }

    /** Gets the name of the cmake installation to use for this build step */
    public String getInstallationName() {
        return this.installationName;
    }

    public void setWorkingDir(String workingDir) {
        this.workingDir = Util.fixEmptyAndTrim(workingDir);
    }

    public String getWorkingDir() {
        return this.workingDir;
    }

    public void setCmakeArgs(String cmakeArgs) {
        this.cmakeArgs = Util.fixEmptyAndTrim(cmakeArgs);
    }

    public String getCmakeArgs() {
        return this.cmakeArgs;
    }

    /**
     * Finds the cmake tool installation to use for this build among all
     * installations configured in the Jenkins administration
     *
     * @return selected CMake installation or {@code null} if none could be
     *         found
     */
    protected CmakeTool getSelectedInstallation() {
        CmakeTool.DescriptorImpl descriptor = (CmakeTool.DescriptorImpl) Jenkins
                .getInstance().getDescriptor(CmakeTool.class);
        for (CmakeTool i : descriptor.getInstallations()) {
            if (installationName != null
                    && i.getName().equals(installationName))
                return i;
        }

        return null;
    }

    /**
     * Constructs a directory under the workspace on the slave.
     *
     * @param path
     *            the directory´s relative path {@code null} to return the workspace directory
     *
     * @return the full path of the directory on the remote machine.
     */
    public static FilePath makeRemotePath(FilePath workSpace, String path) {
        if (path == null) {
            return workSpace;
        }
        FilePath file = workSpace.child(path);
        return file;
    }

    // //////////////////////////////////////////////////////////////////
    // inner classes
    // //////////////////////////////////////////////////////////////////
    /**
     * Descriptor for {@link CmakeBuilder}. Used as a singleton. The class is
     * marked as public so that it can be accessed from views.
     */
    public abstract static class DescriptorImpl
            extends BuildStepDescriptor<Builder> {
        /**
         * the cmake tool installations
         */
        @CopyOnWrite
        private volatile CmakeTool[] installations = new CmakeTool[0];

        public DescriptorImpl(Class<? extends Builder> clazz) {
            super(clazz);
        }

        /**
         * Determines the values of the Cmake installation drop-down list box.
         */
        public ListBoxModel doFillInstallationNameItems() {
            ListBoxModel items = new ListBoxModel();
            CmakeTool.DescriptorImpl descriptor = (CmakeTool.DescriptorImpl) Jenkins
                    .getInstance().getDescriptor(CmakeTool.class);
            for (CmakeTool inst : descriptor.getInstallations()) {
                items.add(inst.getName());// , "" + inst.getPid());
            }
            return items;
        }

        @Override
        public boolean isApplicable(
                @SuppressWarnings("rawtypes") Class<? extends AbstractProject> jobType) {
            // this builder can be used with all kinds of project types
            return true;
        }

    }
}