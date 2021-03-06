package edu.arizona.biosemantics.oto2.ontologize2.client.tree.node;


public class BucketTreeNode extends TextTreeNode {
	
	private String path;

	public BucketTreeNode(String path) {
		super(path);
		this.path = path;
	}

	@Override
	public String getText() {
		if(path.contains("/")) {
			return path.substring(path.lastIndexOf("/") + 1, path.length());
		}
		return path;
	}
	
	public String getPath() {
		return path;
	}	
}