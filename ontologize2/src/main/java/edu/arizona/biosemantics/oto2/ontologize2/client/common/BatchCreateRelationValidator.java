package edu.arizona.biosemantics.oto2.ontologize2.client.common;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.google.gwt.event.shared.GwtEvent;

import edu.arizona.biosemantics.oto2.ontologize2.client.Alerter;
import edu.arizona.biosemantics.oto2.ontologize2.client.ModelController;
import edu.arizona.biosemantics.oto2.ontologize2.client.event.CompositeModifyEvent;
import edu.arizona.biosemantics.oto2.ontologize2.client.event.CreateRelationEvent;
import edu.arizona.biosemantics.oto2.ontologize2.client.event.ReplaceRelationEvent;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.OntologyGraph;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.OntologyGraph.Edge;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.OntologyGraph.Vertex;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.OntologyGraph.Edge.Origin;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.OntologyGraph.Edge.Type;


/**
 * validate the relations created in a batch
 * @author maojin
 *
 */
public class BatchCreateRelationValidator {
	
	public List<Edge> failedEdge = new ArrayList();
	public int totalEdges;
	
	public int getTotalEdges() {
		return totalEdges;
	}

	public void setFailedEdge(List<Edge> failedEdge) {
		this.failedEdge = failedEdge;
	}

	/**
	 * if validate successfullly, create the composite event
	 * otherwise return null
	 * @param batchEdges
	 * @return
	 */
	public CompositeModifyEvent validate(List<Edge> batchEdges) throws Exception{
		OntologyGraph graph = ModelController.getCollection().getGraph();
		this.totalEdges = batchEdges.size();
		if(batchEdges!=null&&batchEdges.size()>0&&validateClosed(batchEdges,graph)&&validateCircle(batchEdges, graph))
			return validateAndCreate(batchEdges,graph);
		else
			return null;
	}
	
	
	/**
	 * validate whether the edges contain any closed
	 * @param event
	 * @return
	 */
	private boolean validateClosed(List<Edge> batchEdges, OntologyGraph graph) {
		//OntologyGraph g = ModelController.getCollection().getGraph();
		for(Edge r : batchEdges) {
			if(graph.isClosedRelations(r.getSrc(), r.getType())) {
				Alerter.showAlert("Create Relation", "Can not create relation for a closed row:"+r.getSrc()) ;
				return false;
			}
		}
		return true;
	}
	
	
	private boolean validateCircle(List<Edge> batchEdges, OntologyGraph graph){
		for(int i=0;i<batchEdges.size();i++) {
			Edge r = batchEdges.get(i);
			boolean formExistedCircle = graph.isCreatesCircular(r);//check the circle definition
			if(formExistedCircle) {
				Alerter.showAlert("Create Relation", "form loop relation between "+r.getSrc()+" and "+ r.getDest()+ " with existed relations ") ;
				return false;
			}
			boolean formAddedCircle = doFormCircleInFuture(batchEdges, i+1,r);
			if(formAddedCircle){
				Alerter.showAlert("Create Relation", "Loop relation between "+r.getSrc()+" and "+ r.getDest() +" in your input") ;
				return false;
			}
		}
		return true;
	}
	
	private boolean doFormCircleInFuture(List<Edge> batchEdges, int start, Edge edge) {
		for(int j=start; j<batchEdges.size();j++){
			Edge potentialRelation =batchEdges.get(j);
			if(edge.getSrc().equals(potentialRelation.getDest())
					&&edge.getDest().equals(potentialRelation.getSrc())
					&&edge.getType().equals(potentialRelation.getType()))
				return true;
		}
		return false;
	}


	/**
	 * validate relations and create composited event
	 * @param batchEdges
	 * @return
	 */
	public CompositeModifyEvent validateAndCreate(List<Edge> batchEdges, OntologyGraph g){
		
		Set handled = new HashSet();//remove duplicated
		
		List<GwtEvent<?>> result = new LinkedList<GwtEvent<?>>();
		
		if(batchEdges.get(0).getType().equals(Type.PART_OF)) {//part of 
			fileterNonSpecific(batchEdges);
		}
		
		filterSynonym(batchEdges);
		
		Set<Vertex> futureContainedClasses = new HashSet<Vertex>();
		Set<Vertex> futureContainedSyns = new HashSet<Vertex>();
		Set<Vertex> futureContainedParents = new HashSet<Vertex>();
		for(Edge e : order(batchEdges)) {
			if(handled.contains(e)||g.existsRelation(e)){//filtered existed
				continue;
			}
			handled.add(e);
			if(e.getType().equals(Type.SYNONYM_OF)) {//synonym
				Edge synonymRootEdge = new Edge(g.getRoot(Type.SYNONYM_OF), e.getSrc(), e.getType(), Origin.USER);
				if(g.isSynonym(e.getSrc())||futureContainedClasses.contains(e.getSrc())){
					failedEdge.add(e);
					continue;
				}
				//ensure the source attaches to root
				if(!g.existsRelation(synonymRootEdge)&&!g.isSynonym(e.getSrc())&&!futureContainedClasses.contains(e.getSrc())){//if the preferred terms does not existed and is not a synonym
					result.add(new CreateRelationEvent(synonymRootEdge));
					futureContainedClasses.add(e.getSrc());
				}
				
				if(!g.isSynonym(e.getDest())){//if the target is not a synonym
					result.add(new CreateRelationEvent(e));
					futureContainedClasses.add(e.getDest());
				}else{/*
					//if the preferred terms does not existed but it is a synonym of another term
					//find the synonym root
					List<Edge> synIn = g.getInRelations(e.getSrc(), Type.SYNONYM_OF);
					if(synIn!=null&&synIn.size()>0){
						Edge preferredEdge = synIn.get(0);
						Vertex preferterm = preferredEdge.getSrc();
						Edge replaceEdge = new Edge(preferterm, e.getDest(), Type.SYNONYM_OF, Origin.USER);
						result.add(new CreateRelationEvent(replaceEdge));
					}*/
					failedEdge.add(e);
				}
				
			} else	if(e.getType().equals(Type.PART_OF)) {//part of 
				try {
					if(g.isValidPartOf(e)){
						if(g.getInRelations(e.getSrc(), Type.PART_OF).size()==0&&!futureContainedParents.contains(e.getSrc())){
							result.add(new CreateRelationEvent(new Edge(g.getRoot(Type.PART_OF), e.getSrc(), e.getType(), Origin.USER)));
						}
						//result.add(new CreateRelationEvent(e));
						Edge exisEdge = null;
						if((exisEdge=checkDest(g, e.getDest(), Type.PART_OF))!=null&&exisEdge.getSrc().equals(g.getRoot(Type.PART_OF))){//
							result.add(new ReplaceRelationEvent(exisEdge, e.getSrc()));
						}else{
							result.add(new CreateRelationEvent(e));
						}
						
						futureContainedParents.add(e.getSrc());
						futureContainedParents.add(e.getDest());
					}else{
						failedEdge.add(e);
					}
				} catch (Exception e1) {
					failedEdge.add(e);
				}
			}else {//class relation
				//if(!"Thing".equals(e.getSrc())&&!g.containsVertex(e.getSrc()) && !futureContained.contains(e.getSrc())){
				if("Thing".equals(e.getSrc().toString())){//add to root
					result.add(new CreateRelationEvent(e));
					//futureContainedSyns.add(e.getSrc());
				}else if(!"Thing".equals(e.getSrc().toString())&&!futureContainedSyns.contains(e.getSrc())&&g.getInRelations(e.getSrc(), Type.SUBCLASS_OF).size()==0){//source has not been attached
					//if the superclass doesnot exist in the tree and in future contained set, add to root
					Edge superclassRootEdge = new Edge(g.getRoot(e.getType()), 
							e.getSrc(), e.getType(), Origin.USER);
					futureContainedSyns.add(e.getSrc());
					result.add(new CreateRelationEvent(superclassRootEdge));
					Edge exisEdge = null;
					if((exisEdge=checkDest(g, e.getDest(), Type.SUBCLASS_OF))!=null){//if the dest of the new relation is a dest of a relation to "Thing", create a replace relation instead
						result.add(new ReplaceRelationEvent(exisEdge, e.getSrc()));
					}else{
						result.add(new CreateRelationEvent(e));
					}
				}else if(!"Thing".equals(e.getSrc().toString())&&
						(g.getInRelations(e.getSrc(), Type.SUBCLASS_OF).size()>0||futureContainedSyns.contains(e.getSrc()))){
					//if the superclass doesnot exist in the tree but in future contained set, add to root
					Edge exisEdge = null;
					if((exisEdge=checkDest(g, e.getDest(), Type.SUBCLASS_OF))!=null){//if the dest of the new relation is a dest of a relation to "Thing", create a replace relation instead
						result.add(new ReplaceRelationEvent(exisEdge, e.getSrc()));
					}else{
						result.add(new CreateRelationEvent(e));
					}
				}
				/*else if(g.getInRelations(e.getSrc(), Type.SUBCLASS_OF).size()>0){//add to graph
					result.add(new CreateRelationEvent(e));
				}*/
				futureContainedSyns.add(e.getDest());
			}
		}
		
		return new CompositeModifyEvent(result);
	}
	
	
	private Edge checkDest(OntologyGraph g, Vertex dest, Type type) {
		List<Edge> inedges = g.getInRelations(dest, type);
		if(inedges.size()==1&&inedges.get(0).getSrc().equals(g.getRoot(type))){
			return inedges.get(0);
		}
		return null;
	}

	
	//one node can not be parts of two parents
	private void fileterNonSpecific(List<Edge> batchEdges) {
		Set target = new HashSet();
		OntologyGraph graph = ModelController.getCollection().getGraph();
		List<Vertex> parents = graph.getAllDestinations(graph.getRoot(Type.PART_OF), Type.PART_OF);
		for(Vertex p:parents){
			List<Vertex> out = graph.getAllDestinations(p, Type.PART_OF);
			for(Vertex o:out) target.add(o.getValue());
		}
		Set duplicate = new HashSet();
		for(int i=0;i<batchEdges.size();i++){
			Edge e= batchEdges.get(i);
			if(!e.getType().equals(Type.PART_OF)) continue;
			if(target.contains(e.getDest().getValue())){//move out to duplicate target set
				duplicate.add(e.getDest().getValue());
			}
			target.add(e.getDest().getValue());
		}
		
		for(int i=0;i<batchEdges.size();){
			Edge e= batchEdges.get(i);
			if(!e.getType().equals(Type.PART_OF)){
				i++;
				continue;
			}
			if(duplicate.contains(e.getDest().getValue())){//move out to duplicate target set
				this.failedEdge.add(e);
				batchEdges.remove(e);
			}else{
				i++;
			}
		}
	}

	//one node can not be parts of two parents
	private void filterSynonym(List<Edge> batchEdges) {
		Set target = new HashSet();
		OntologyGraph graph = ModelController.getCollection().getGraph();
		List<Vertex> parents = graph.getAllDestinations(graph.getRoot(Type.SYNONYM_OF), Type.SYNONYM_OF);
		for(Vertex p:parents){
			List<Vertex> out = graph.getAllDestinations(p, Type.SYNONYM_OF);
			for(Vertex o:out) target.add(o.getValue());
		}
		Set duplicate = new HashSet();
		for(int i=0;i<batchEdges.size();i++){
			Edge e= batchEdges.get(i);
			if(!e.getType().equals(Type.SYNONYM_OF)) continue;
			if(target.contains(e.getDest().getValue())){//move out to duplicate target set
				duplicate.add(e.getDest().getValue());
			}
			target.add(e.getDest().getValue());
		}
		
		for(int i=0;i<batchEdges.size();){
			Edge e= batchEdges.get(i);
			if(!e.getType().equals(Type.SYNONYM_OF)){
				i++;
				continue;
			}
			if(duplicate.contains(e.getDest().getValue())){//move out to duplicate target set
				this.failedEdge.add(e);
				batchEdges.remove(e);
			}else{
				i++;
			}
		}
		
		for(int i=0;i<batchEdges.size();){
			Edge e= batchEdges.get(i);
			if(e.getType().equals(Type.SYNONYM_OF)){
				i++;
				continue;
			}
			if(graph.isSynonym(e.getSrc())||graph.isSynonym(e.getDest())){//if the edge contains synonym terms, don't add it
				this.failedEdge.add(e);
				batchEdges.remove(e);
			}else{
				i++;
			}
		}
		
	}
	
	private List<Edge> order(List<Edge> edges) {
		List<Edge> copy = new ArrayList<Edge>(edges);
		List<Edge> result = new LinkedList<Edge>(); 
		Set<Vertex> contained = new HashSet<Vertex>();
		boolean first = true;
		while(!copy.isEmpty()) {
			Iterator<Edge> it = copy.iterator();
			while(it.hasNext()) {
				Edge e = it.next();
				if(contained.contains(e.getSrc())) {
					it.remove();
					result.add(e);
					contained.add(e.getDest());
				} else if(first) {
					boolean incomingEdge = false;
					for(Edge e2 : copy) {
						if(!e2.equals(e) && e2.getDest().equals(e.getSrc())) {
							incomingEdge = true;
							break;
						}
					}
					if(!incomingEdge) {
						it.remove();
						result.add(e);
						contained.add(e.getSrc());
						contained.add(e.getDest());
					}
				}
			}
			first = false;
		}
		return result;
	}

	public List<Edge> getFailedEdge() {
		return this.failedEdge;
	}

}
