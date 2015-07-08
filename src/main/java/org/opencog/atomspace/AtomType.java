package org.opencog.atomspace;

import com.google.common.base.CaseFormat;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * <p>i.e. {@code ConceptNode}, {@code GeneNode}, {@code PredicateNode}, {@code PhraseNode}, {@code WordNode}, etc.</p>
 *
 * <p>Hendy's proposal: The AtomType is a Neo4j label of a {@link Node} or {@link Link}.
 * Most {@link Link}s .</p>
 *
 * @todo Determine what's the best way to implement Atom types, in a way that is performant
 * (both Java-wise and DB-wise) and is relatively portable in the Neo4j database.
 * (i.e. autogenerated numeric identifiers may be problematic)
 */
public enum AtomType {
    ATOM(null, null),
    NODE(GraphMapping.VERTEX, null),
    LINK(GraphMapping.HYPEREDGE, null),
    WORD_NODE(GraphMapping.VERTEX, "opencog_WordNode"),
    CONCEPT_NODE(GraphMapping.VERTEX, "opencog_ConceptNode"),
    ASSOCIATIVE_LINK(GraphMapping.HYPEREDGE, "opencog_AssociativeLink"),
    EVALUATION_LINK(GraphMapping.HYPEREDGE, "opencog_EvaluationLink"),
    MULTIPARENT_LINK(GraphMapping.HYPEREDGE, "opencog_MultiparentLink"),
    /**
     * rdfs_subClassOf relationship
     */
    INHERITANCE_LINK(GraphMapping.EDGE, "rdfs_subClassOf"), // WARNING: impossible to link to an InheritanceLink!
    /**
     * rdf_type relationship
     */
    MEMBER_LINK(GraphMapping.EDGE, "rdf_type"), // WARNING: impossible to link to a MemberLink!
    GENE_NODE(GraphMapping.VERTEX, "opencog_GeneNode"),
    PREDICATE_NODE(GraphMapping.VERTEX, "opencog_PredicateNode"),
    PHRASE_NODE(GraphMapping.VERTEX, "opencog_PhraseNode");

    static final ImmutableBiMap<Integer, AtomType> atomTypeInfos;

    static {
        final HashMap<Integer, AtomType> atomTypeInfob = new HashMap<>();
        Arrays.stream(values())
                .forEach(it -> atomTypeInfob.put(atomTypeInfob.size() + 1, it)); // must NOT be concurrent!
        atomTypeInfos = ImmutableBiMap.copyOf(atomTypeInfob);
    }

    private GraphMapping graphMapping;
    private String graphLabel;

    AtomType(GraphMapping graphMapping, String graphLabel) {
        this.graphMapping = graphMapping;
        this.graphLabel = graphLabel;
    }

    public GraphMapping getGraphMapping() {
        return graphMapping;
    }

    public String getGraphLabel() {
        return graphLabel;
    }

    public String toUpperCamel() { return CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, name()); }

    public List<EdgeMapping> getEdgeMappings(int outgoingCount) {
        final ImmutableList.Builder<EdgeMapping> mappingb = ImmutableList.builder();
        if (this == EVALUATION_LINK) {
            mappingb.add(new EdgeMapping("opencog_predicate"));
            for (int i = 0; i < outgoingCount - 1; i++) {
                mappingb.add(new EdgeMapping("opencog_parameter", ImmutableMap.of("position", i)));
            }
        } else {
            for (int i = 0; i < outgoingCount; i++) {
                mappingb.add(new EdgeMapping("opencog_parameter", ImmutableMap.of("position", i)));
            }
        }
        return mappingb.build();
    }

    public static AtomType forGraphLabel(String label) {
        for (final AtomType atomType : AtomType.values()) {
            if (atomType.getGraphLabel() != null && atomType.getGraphLabel().equals(label)) {
                return atomType;
            }
        }
        throw new IllegalArgumentException("Unknown AtomType for label '" + label + "'");
    }

    public static AtomType forUpperCamel(String camelType) {
        return valueOf(CaseFormat.UPPER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, camelType));
    }

    public int getId() {
        return atomTypeInfos.inverse().get(this);
    }

    public AtomType forId(int id) {
        return atomTypeInfos.get(id);
    }


}
