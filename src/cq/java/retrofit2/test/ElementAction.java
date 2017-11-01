package cq.java.retrofit2.test;

public class ElementAction {
	public int id;
	public String name;
	public String toString(){
		return String.format("%8d:\t%s", id, name);
	}
}
