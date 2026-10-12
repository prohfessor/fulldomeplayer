package dnd;

import java.io.File;
import java.util.Arrays;
import javax.swing.tree.DefaultMutableTreeNode;

class MediaFileNode extends DefaultMutableTreeNode {
    private final File mFile;
    private boolean flagExt = false;
    private String[] allowedExt = new String[0];

    public MediaFileNode(final File pFile) {
        mFile = pFile;
    }

    public void setAllowedExt(String[] exts) {
	allowedExt = exts == null ? new String[0] : exts.clone();
    }
    public void setFlagExt(boolean fae) {flagExt=fae;}

    public boolean isDirectory() {
        return mFile.isDirectory();
    }

    public MediaFileNode[] listFiles() {
        final File[] files = mFile.listFiles();
        if (files == null) return new MediaFileNode[0];
        if (files.length < 1) return new MediaFileNode[0];
	int length_files=0;

        final MediaFileNode[] ret = new MediaFileNode[files.length];


        for (int i = 0; i < ret.length; i++) {
            final File f = files[i];
//	System.out.println("-- "+f.toString());
	if(flagExt && allowedExt.length>0 && !f.isDirectory()) {
		String s=f.toString();
	        String ext = null;
//	System.out.println("-- "+f);
	        int ii = s.lastIndexOf('.');
	        if (ii > 0 &&  ii < s.length() - 1) {
	            ext = s.substring(ii+1).toLowerCase();
	        }
		if (ext!= null) {
			for(int j=0; j<allowedExt.length; j++) {
		        	if (ext.equals(allowedExt[j])) {
					ret[length_files++]=createChild(f);
					
//	System.out.println("______________ ");
		        	} 
			}
		}
	} else  ret[length_files++] = createChild(f);
	} //for
	
        final MediaFileNode[] nret = new MediaFileNode[length_files];
	System.arraycopy(ret, 0, nret, 0, length_files);

        return nret;
    }

    /** Child nodes inherit the active filter so it applies inside all folders. */
    private MediaFileNode createChild(File file) {
        MediaFileNode child = new MediaFileNode(file);
        child.setFlagExt(flagExt);
        child.setAllowedExt(allowedExt);
        return child;
    }

    public File getFile() {
        return mFile;
    }

    @Override public String toString() {
        return mFile.getName();
    }
} 
