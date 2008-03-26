<?xml version="1.0" encoding="UTF-8"?>
<!--
    - JGraLab - The Java graph laboratory
    - (c) 2006-2007 Institute for Software Technology
    -               University of Koblenz-Landau, Germany
    -
    -               ist@uni-koblenz.de
    -
    - Please report bugs to http://serres.uni-koblenz.de/bugzilla
    -
    - This program is free software; you can redistribute it and/or
    - modify it under the terms of the GNU General Public License
    - as published by the Free Software Foundation; either version 2
    - of the License, or (at your option) any later version.
    -
    - This program is distributed in the hope that it will be useful,
    - but WITHOUT ANY WARRANTY; without even the implied warranty of
    - MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    - GNU General Public License for more details.
    -
    - You should have received a copy of the GNU General Public License
    - along with this program; if not, write to the Free Software
    - Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
    xmlns:uml="http://schema.omg.org/spec/UML/2.1"
    xmlns:xmi="http://schema.omg.org/spec/XMI/2.1"
    xmlns:thecustomprofile="http://www.sparxsystems.com/profiles/thecustomprofile/1.0"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:myfunctions="http://www.myfunctions.de"
    version="2.0">
    
    <xsl:output method="text"/>
    
    <!-- Specifies if ids of associations should be added to EdgeClassNames
        . This helps to avoid duplicate names, but makes code harder to understand -->
    <xsl:param name="appendEdgeIds" as="xs:string" required="no" select="'no'"/>
    <!-- specifies whether to perform some automatic corrections
        these include:
        conversion of first char in class and association names to uppercase, 
        change of identifiers in order to avoid conflicts with reserved words,
        creation of EdgeClass names by using role or VertexClass names, if corresponding association has no name -->
    <xsl:param name="autoCorrect" required="no" select="'yes'"/>
    <!-- Specifies names of classes which shall be transformed to EdgeClasses in the tg-file. This also applies to their subclasses.
        Such a class must have two associations: one with the role name "source" at the from-Class (the "from" side of the resulting EdgeClass) 
        and one with the role name "target" at the to-Class (the "to" side of the resulting EdgeClass) -->
    <xsl:param name="classToEdgeClass" required="no"/>
    <!-- specifies if some errors shall be detected and the transformation be aborted 
        the errors include:
        detection of classes without names, 
        detection of classes with duplicate names,
        detection of xmi:idrefs without corresponding xmi:ids, e.g. if a class which is in the schema has an associated class not in the schema -->
    <xsl:param name="errorDetection" required="no" select="'yes'"/>
    <!-- Specifies if names of EdgeClasses should be FromRolenameLinksToToRolename or simply
        LinksToToRolename. If set to yes, the extended form FromRolenameLinksToToRolename
        is used, otherwise the simpler form LinksToToRolename. -->
    <xsl:param name="extendedEdgeClassNames" as="xs:string" required="no" select="'no'"/>
    <!-- specifies the Schema name; must correspond to name of package containing the schema -->
    <xsl:param name="schemaName" required="yes"/>
    <!-- specifies tool used to create the XMI-file -->
    <xsl:param name="tool" required="no"/>
    <!-- specifies whether 
        (1) a subset constraint of an association results in the corresponding EdgeClass to be
        a specialization of the EdgeClass corresponding to the subsetted association,
        (2) a derived union constraint of an association results in the corresponding EdgeClass
        to be abstract 
        WARNING: Using this feature together with specialization of associated classes probably
        results in corrupted TG files. -->
    <xsl:param name="uml" required="no" select="'no'"/>
    
    <xsl:variable name="reservedWords" select="
        'abstract', 'aggregate', 'AggregationClass', 'Boolean', 'CompositionClass', 'Double', 'EdgeClass', 'EnumDomain', 'f', 'from',
        'Graph', 'GraphClass', 'Integer', 'List', 'Long', 'Object', 'Package', 'RecordDomain', 'role', 'Schema', 'Set', 'String', 'to', 't', 'VertexClass'" 
        as="xs:string*"/>
    
    <!-- stores the package with the given schemaName -->
    <xsl:variable name="schemaPackage" select="xmi:XMI/uml:Model//packagedElement[@xmi:type='uml:Package' and @name=$schemaName]"/>
    
    <!-- processes root -->
    <xsl:template match="/">
        
        <!-- check is Schema is self-contained -->
        <xsl:if test="$errorDetection = 'yes'">
            <xsl:apply-templates select="$schemaPackage//@xmi:idref"/>
        </xsl:if>
        
        <!-- convert notes not related to any element to comments -->
        <xsl:apply-templates select="$schemaPackage//ownedComment[empty(annotatedElement) and exists(@body)]"/>

        <!-- write schema name -->
        <xsl:call-template name="schema"/>
        
        <!-- write graph class -->
        <xsl:if test="$schemaPackage//packagedElement[@xmi:type='uml:Class'
                and exists(index-of(/xmi:XMI/uml:Model/thecustomprofile:GraphClass/@base_Class, @xmi:id))]">
            <xsl:apply-templates select="$schemaPackage//packagedElement[@xmi:type='uml:Class'
                and exists(index-of(/xmi:XMI/uml:Model/thecustomprofile:GraphClass/@base_Class, @xmi:id))]"/>    
        </xsl:if>
        <xsl:if test="empty(/xmi:XMI/uml:Model/thecustomprofile:GraphClass/@base_Class)">
            <xsl:text>GraphClass DefaultGraphClass;&#xa;</xsl:text>    
        </xsl:if>

        <!-- convert to EnumDomains -->
        <xsl:apply-templates select="$schemaPackage//packagedElement[@xmi:type='uml:Enumeration']"/>
        <!-- convert to RecordDomains -->
        <xsl:apply-templates select="$schemaPackage//packagedElement[@xmi:type = 'uml:Class'
            and exists(index-of(/xmi:XMI/uml:Model//thecustomprofile:record/@base_Class, @xmi:id))]"/>
        <!-- convert to Packages -->
        <xsl:apply-templates select="$schemaPackage//packagedElement[@xmi:type='uml:Package']"/>
        <!-- write default package -->
        <xsl:text>Package;&#xa;</xsl:text>
        <!-- convert to VertexClasses -->
        <xsl:apply-templates select="$schemaPackage//packagedElement[@xmi:type='uml:Class' and empty(myfunctions:getAssociation(.)) 
            and not(myfunctions:isClassToEdgeClass(.))
            and empty(index-of(/xmi:XMI/uml:Model//thecustomprofile:record/@base_Class, @xmi:id))
            and empty(index-of(/xmi:XMI/uml:Model//thecustomprofile:GraphClass/@base_Class, @xmi:id))]"/>
        <!-- convert to EdgeClasses -->
        <xsl:apply-templates select="$schemaPackage//packagedElement[(@xmi:type='uml:Association' or @xmi:type='uml:AssociationClass') 
            and not(myfunctions:isClassToEdgeClassAssociation(.))
            or @xmi:type = 'uml:Class' and exists(myfunctions:getAssociation(.)) and not(myfunctions:isClassToEdgeClass(.))]"/>
        <xsl:apply-templates select="$schemaPackage//packagedElement[@xmi:type='uml:Class' and myfunctions:isClassToEdgeClass(.)]"/>
    </xsl:template>
    
    <!-- check is Schema is self-contained -->
    <xsl:template match="@xmi:idref">
        <xsl:if test="empty(index-of($schemaPackage//@xmi:id, current()))">
            <xsl:value-of select="error(QName('', 'xmi2tg-Error'), concat('schema is not self-contained, caused by xmi:idref ', current()))"/>
        </xsl:if>      
    </xsl:template>
    
    <!-- converts notes to comments -->
    <xsl:template match="ownedComment">
        <xsl:text>// </xsl:text>
        <xsl:value-of select="myfunctions:adaptNotes(@body)"/>
        <xsl:text>&#xa;</xsl:text>
    </xsl:template>
    
    <!-- converts constraints to comments -->
    <xsl:template match="ownedRule">
        <xsl:text>// {</xsl:text>
        <xsl:value-of select="myfunctions:adaptNotes(specification/@body)"/>
        <xsl:text>} &#xa;</xsl:text>
    </xsl:template>
    
    <!-- writes schema name -->
    <xsl:template name="schema">
        <xsl:text>Schema </xsl:text>
        <xsl:value-of select="$schemaName"/>
        <xsl:text>;&#xa;</xsl:text>      
    </xsl:template>
    
    <xsl:template match="packagedElement[@xmi:type='uml:Class'
            and exists(index-of(/xmi:XMI/uml:Model/thecustomprofile:GraphClass/@base_Class, @xmi:id))]">
        <xsl:text>GraphClass </xsl:text>
        
        <xsl:if test="$autoCorrect = 'yes'">
            <xsl:value-of select="myfunctions:correctQualifiedName(@name)"/>
        </xsl:if>
        <xsl:if test="$autoCorrect = 'no'">
            <xsl:value-of select="@name"/>
        </xsl:if>
       
        <!-- GraphClass attributes -->
        <xsl:apply-templates select="$schemaPackage/packagedElement[@xmi:id=/xmi:XMI/uml:Model/thecustomprofile:GraphClass/@base_Class and @xmi:type='uml:Class']/ownedAttribute[not(@association)]">
            <xsl:with-param name="caller">attributedElementClass</xsl:with-param>
        </xsl:apply-templates>
        <xsl:text>;&#xa;</xsl:text>  
    </xsl:template>
    
    <!-- creates EnumDomain -->
    <xsl:template match="packagedElement[@xmi:type='uml:Enumeration']">
        
        <!-- convert notes and constraints to comments -->
        <xsl:apply-templates select="$schemaPackage//ownedComment[annotatedElement/@xmi:idref = current()/@xmi:id]"/>
        <xsl:apply-templates select="$schemaPackage//ownedRule[constrainedElement/@xmi:idref = current()/@xmi:id]"/>
        
        <xsl:text>EnumDomain </xsl:text>
        <xsl:if test="$autoCorrect = 'yes'">
            <xsl:value-of select="myfunctions:correctQualifiedName(myfunctions:getQualifiedName(., $schemaPackage))"/>
        </xsl:if>
        <xsl:if test="$autoCorrect = 'no'">
            <xsl:value-of select="myfunctions:getQualifiedName(., $schemaPackage)"/>
        </xsl:if>
        <xsl:apply-templates select="ownedAttribute">
            <xsl:with-param name="caller">enum</xsl:with-param>
        </xsl:apply-templates>
        <xsl:text>;&#xa;</xsl:text>
    </xsl:template>
    
    <!-- creates RecordDomain -->
    <xsl:template match="packagedElement[@xmi:type = 'uml:Class' and empty(myfunctions:getAssociation(.)) and not(myfunctions:isClassToEdgeClass(.))
            and exists(index-of(/xmi:XMI/uml:Model//thecustomprofile:record/@base_Class, @xmi:id))]">
        
        <!-- convert notes and constraints to comments -->
        <xsl:apply-templates select="$schemaPackage//ownedComment[annotatedElement/@xmi:idref = current()/@xmi:id]"/>
        <xsl:apply-templates select="$schemaPackage//ownedRule[constrainedElement/@xmi:idref = current()/@xmi:id]"/>
        
        <xsl:text>RecordDomain </xsl:text>
        <xsl:if test="$autoCorrect = 'yes'">
            <xsl:value-of select="myfunctions:correctQualifiedName(myfunctions:getQualifiedName(., $schemaPackage))"/>
        </xsl:if>
        <xsl:if test="$autoCorrect = 'no'">
            <xsl:value-of select="myfunctions:getQualifiedName(., $schemaPackage)"/>
        </xsl:if>
        <xsl:apply-templates select="ownedAttribute">
            <xsl:with-param name="caller">record</xsl:with-param>
        </xsl:apply-templates>
        <xsl:text>;&#xa;</xsl:text>
    </xsl:template>
    
    <!-- create Package -->
    <xsl:template match="packagedElement[@xmi:type='uml:Package']">       
        <!-- convert notes and constraints to comments -->
        <xsl:apply-templates select="$schemaPackage//ownedComment[annotatedElement/@xmi:idref = current()/@xmi:id]"/>
        <xsl:apply-templates select="$schemaPackage//ownedRule[constrainedElement/@xmi:idref = current()/@xmi:id]"/>
        
        <xsl:text>Package </xsl:text>
        
        <!-- create name of Package -->
        <xsl:if test="$autoCorrect = 'yes'">
            <xsl:value-of select="lower-case(myfunctions:getQualifiedName(., $schemaPackage))"/>
        </xsl:if>
        <xsl:if test="$autoCorrect = 'no'">
            <xsl:value-of select="myfunctions:getQualifiedName(., $schemaPackage)"/>
        </xsl:if>
        <xsl:text>;&#xa;</xsl:text>
    </xsl:template>
    
    <!-- creates VertexClass -->
    <xsl:template match="packagedElement[@xmi:type = 'uml:Class' and empty(myfunctions:getAssociation(.)) and not(myfunctions:isClassToEdgeClass(.))
        and empty(index-of(/xmi:XMI/uml:Model//thecustomprofile:record/@base_Class, @xmi:id))
        and empty(index-of(/xmi:XMI/uml:Model//thecustomprofile:GraphClass/@base_Class, @xmi:id))]">
        
        <!-- convert notes and constraints to comments -->
        <xsl:apply-templates select="$schemaPackage//ownedComment[annotatedElement/@xmi:idref = current()/@xmi:id]"/>
        <xsl:apply-templates select="$schemaPackage//ownedRule[constrainedElement/@xmi:idref = current()/@xmi:id]"/>
        
        <xsl:if test="@isAbstract='true'">
            <xsl:text>abstract </xsl:text>
        </xsl:if>
        <xsl:text>VertexClass </xsl:text>        
        
        <!-- create name of VertexClass -->
        <xsl:if test="$autoCorrect = 'yes'">
            <xsl:value-of select="myfunctions:correctQualifiedName(myfunctions:getQualifiedName(., $schemaPackage))"/>
        </xsl:if>
        <xsl:if test="$autoCorrect = 'no'">
            <xsl:value-of select="myfunctions:getQualifiedName(., $schemaPackage)"/>
        </xsl:if>
        
        <xsl:if test="$errorDetection = 'yes'">
            <xsl:if test="empty(@name)">
                <xsl:value-of select="error(QName('', 'xmi2tg-Error'), 'empty class name')"/>
            </xsl:if>
            <xsl:if test="count(current()/parent::packagedElement/packagedElement[@name = current()/@name]) > 1">
                <xsl:value-of select="error(QName('', 'xmi2tg-Error'), concat('duplicate vertex name ''', @name, ''' in package ''', current()/parent::packagedElement, ''''))"/>   
            </xsl:if>  
        </xsl:if>
        
        <xsl:call-template name="generalization">
            <xsl:with-param name="elements" select="$schemaPackage//packagedElement[@xmi:id = current()/generalization/@general]"/>
        </xsl:call-template>
        
        <xsl:apply-templates select="ownedAttribute[not(@association)]">
            <xsl:with-param name="caller">attributedElementClass</xsl:with-param>
        </xsl:apply-templates>
        <xsl:text>;&#xa;</xsl:text>
    </xsl:template>
    
    <!-- creates EdgeClass, AggregationClass or CompositionClass -->
    <xsl:template match="packagedElement[(@xmi:type='uml:Association' or @xmi:type='uml:AssociationClass') 
        and not(myfunctions:isClassToEdgeClassAssociation(.)) 
        or @xmi:type = 'uml:Class' and exists(myfunctions:getAssociation(.)) and not(myfunctions:isClassToEdgeClass(.))]">
        
        <!-- convert notes and constraints to comments -->
        <xsl:apply-templates select="$schemaPackage//ownedComment[annotatedElement/@xmi:idref = current()/@xmi:id]"/>
        <xsl:apply-templates select="$schemaPackage//ownedRule[constrainedElement/@xmi:idref = current()/@xmi:id]"/>
        
        <!-- if current() is of type 'uml:Class', this variable stores its generalization which must be an AssociationClass -->
        <xsl:variable name="association" select="myfunctions:getAssociation(.)"/>
        
        <!-- store XPaths to aggregate attribute of source VertexClass -->
        <xsl:variable name="fromAggregateAttribute" select="$schemaPackage//packagedElement[$association/ownedEnd/type/@xmi:idref = @xmi:id
            or ownedAttribute/@xmi:id = $association/memberEnd/@xmi:idref]/ownedAttribute[@association = $association/@xmi:id 
                and contains(@xmi:id, 'src')]/@aggregation 
                union $association/ownedEnd[contains(@xmi:id, 'src')]/@aggregation"/>
        
        <!-- store XPaths to aggregate attribute of target VertexClass -->
        <xsl:variable name="toAggregateAttribute" select="$schemaPackage//packagedElement[$association/ownedEnd/type/@xmi:idref = @xmi:id
            or ownedAttribute/@xmi:id = $association/memberEnd/@xmi:idref]/ownedAttribute[@association = $association/@xmi:id 
                and contains(@xmi:id, 'dst')]/@aggregation 
            union $association/ownedEnd[contains(@xmi:id, 'dst')]/@aggregation"/>
        
        <!-- store XPaths to aggregate attribute of both source and target VertexClasses-->
        <xsl:variable name="aggregateAttributes" select="$fromAggregateAttribute union $toAggregateAttribute"/> 
                
        <!-- "from" VertexClass 
            The expression says that either the id of a type of an ownedEnd of the currently
            processed EdgeClass with an id containing 'src' must be equal to the id of the
            looked-for packagedElement, or that the id of an ownedAttribute of the looked-for
            packagedElement must contain 'src' and be equal to the id of a memberEnd of the
            currently processed EdgeClass. -->
        <xsl:variable name="fromVertexClass" select="$schemaPackage//packagedElement[$association/ownedEnd[contains(@xmi:id, 'src')]/type/@xmi:idref = @xmi:id
            or $schemaPackage//packagedElement/ownedAttribute[contains(@xmi:id, 'src') and @xmi:id = $association/memberEnd/@xmi:idref]/type/@xmi:idref = @xmi:id]"/>
            
        <!-- name of "from" VertexClass -->
        <xsl:variable name="fromVertexClassName" select="if ($autoCorrect='yes')
            then myfunctions:correctQualifiedName(myfunctions:getQualifiedName($fromVertexClass, $schemaPackage)) else myfunctions:getQualifiedName($fromVertexClass, $schemaPackage)"/>
        
        <!-- "to" VertexClass 
            The expression says that either the id of a type of an ownedEnd of the currently
            processed EdgeClass with an id containing 'dst' must be equal to the id of the
            looked-for packagedElement, or that the id of an ownedAttribute of the looked-for
            packagedElement must contain 'dst' and be equal to the id of a memberEnd of the
            currently processed EdgeClass. -->
        <xsl:variable name="toVertexClass" select="$schemaPackage//packagedElement[$association/ownedEnd[contains(@xmi:id, 'dst')]/type/@xmi:idref = @xmi:id
            or $schemaPackage//packagedElement/ownedAttribute[contains(@xmi:id, 'dst') and @xmi:id = $association/memberEnd/@xmi:idref]/type/@xmi:idref = @xmi:id]"/>
        
        <!-- name of "to" VertexClass -->
        <xsl:variable name="toVertexClassName" select="if ($autoCorrect='yes')
            then myfunctions:correctQualifiedName(myfunctions:getQualifiedName($toVertexClass, $schemaPackage)) else myfunctions:getQualifiedName($toVertexClass, $schemaPackage)"/>
        
        <!-- name of "from" role
            The expression says that the name attribute of the ownedAttribute corresponding to the association source (non-navigable end) shall be concatenated with the name
            attribute of the ownedEnd corresponding to the association source. Since one of these strings is empty, the result is the "from"-rolename. -->
        <xsl:variable name="fromRoleName" select="concat($schemaPackage//ownedAttribute[contains(@xmi:id, 'src') and @xmi:id = $association/memberEnd/@xmi:idref]/@name,
            $association/ownedEnd[contains(@xmi:id, 'src')]/@name)"/>
        
        <!-- name of "to" role
            The expression says that the name attribute of the ownedAttribute corresponding to the association destination (non-navigable end) shall be concatenated with the name
            attribute of the ownedEnd corresponding to the association destination. Since one of these strings is empty, the result is the "to"-rolename. -->
        <xsl:variable name="toRoleName" select="concat($schemaPackage//ownedAttribute[contains(@xmi:id, 'dst') and @xmi:id = $association/memberEnd/@xmi:idref]/@name,
            $association/ownedEnd[contains(@xmi:id, 'dst')]/@name)"/>
        
        <xsl:if test="$uml='yes'">
            <!-- If the association is a derived union or the association class is abstract, the EdgeClass to be created has to be abstract -->     
            <xsl:if test="ownedEnd/@isDerivedUnion='true' or $schemaPackage//packagedElement/ownedAttribute[@association = $association/@xmi:id]/@isDerivedUnion = 'true'">
                <xsl:text>abstract </xsl:text>
            </xsl:if>
        </xsl:if>
        <xsl:if test="@isAbstract = 'true'">
            <xsl:text>abstract </xsl:text>
        </xsl:if>
        
        <!-- determine exact type on the basis of aggregate attributes -->
        <xsl:choose>
            <xsl:when test="every $aggr in $aggregateAttributes satisfies $aggr = 'none'">
                <xsl:text>EdgeClass </xsl:text>
            </xsl:when>
            <xsl:when test="some $aggr in $aggregateAttributes satisfies $aggr = 'shared'">
                <xsl:text>AggregationClass </xsl:text>
            </xsl:when>
            <xsl:when test="some $aggr in $aggregateAttributes satisfies $aggr = 'composite'">
                <xsl:text>CompositionClass </xsl:text>
            </xsl:when>
        </xsl:choose>
        
        <!-- create edge class name -->
        <xsl:call-template name="edgeClassName">
            <xsl:with-param name="association" select="."/>
        </xsl:call-template>
        
        <!-- provide super classes of EdgeClass -->
        <xsl:if test="$uml='no'">
            <xsl:call-template name="generalization">
                <xsl:with-param name="elements" select="$schemaPackage//packagedElement[@xmi:id = current()/generalization/@general] 
                    union $schemaPackage[memberEnd/@xmi:idref = current()/ownedEnd/redefinedProperty/@xmi:idref]
                    union $schemaPackage//packagedElement[memberEnd/@xmi:idref = $schemaPackage//packagedElement/ownedAttribute[@association = current()/@xmi:id]/redefinedProperty/@xmi:idref]"/>
            </xsl:call-template>
        </xsl:if>
        <xsl:if test="$uml='yes'">
            <xsl:call-template name="generalization">
                <xsl:with-param name="elements" select="$schemaPackage//packagedElement[memberEnd/@xmi:idref = current()/ownedEnd/redefinedProperty/@xmi:idref]
                    union $schemaPackage//packagedElement[memberEnd/@xmi:idref = $schemaPackage//packagedElement/ownedAttribute[@association = current()/@xmi:id]/redefinedProperty/@xmi:idref]
                    union $schemaPackage//packagedElement[memberEnd/@xmi:idref = current()/ownedEnd/subsettedProperty/@xmi:idref]
                    union $schemaPackage//packagedElement[memberEnd/@xmi:idref = $schemaPackage//packagedElement/ownedAttribute[@association = current()/@xmi:id]/subsettedProperty/@xmi:idref]"/>
            </xsl:call-template> 
        </xsl:if>
        
        <xsl:text> from </xsl:text>         
        <xsl:value-of select="$fromVertexClassName"/>
        
        <!-- "from" multiplicity -->
        <xsl:call-template name="multiplicity">
            <xsl:with-param name="side" select="'src'"/>
            <xsl:with-param name="association" select="$association"/>
        </xsl:call-template>
     
        <!-- "from" role name -->
        <xsl:call-template name="role">
            <xsl:with-param name="rolename" select="$fromRoleName"/>
        </xsl:call-template>
        
        <!-- redefinitions on "from" side -->
        <xsl:apply-templates select="ownedEnd[contains(@xmi:id, 'src')]/redefinedProperty union $schemaPackage//packagedElement/ownedAttribute[contains(@xmi:id, 'src') and @association = $association/@xmi:id]/redefinedProperty" mode="redefines"/>
                        
        <xsl:text> to </xsl:text>        
        <xsl:value-of select="$toVertexClassName"/>
        
        <!-- "to" multiplicity -->
        <xsl:call-template name="multiplicity">
            <xsl:with-param name="side" select="'dst'"/>
            <xsl:with-param name="association" select="$association"/>
        </xsl:call-template>
        
        <!-- "to" role name -->
        <xsl:call-template name="role">
            <xsl:with-param name="rolename" select="$toRoleName"/>
        </xsl:call-template>
        
        <!-- redefinitions on "to" side -->
        <xsl:apply-templates select="ownedEnd[contains(@xmi:id, 'dst')]/redefinedProperty union $schemaPackage//packagedElement/ownedAttribute[contains(@xmi:id, 'dst') and @association = $association/@xmi:id]/redefinedProperty" mode="redefines"/>
        
        <!-- if current EdgeClass is an AggregationClass or CompositionClass, determine aggregate VertexClass -->
        <xsl:if test="$fromAggregateAttribute != 'none'">
            <xsl:text> aggregate from</xsl:text>
        </xsl:if>
        <xsl:if test="$toAggregateAttribute != 'none'">
            <xsl:text> aggregate to</xsl:text>
        </xsl:if>
        
        <!-- attributes -->
        <xsl:apply-templates select="ownedAttribute[not(@association)]">
            <xsl:with-param name="caller">attributedElementClass</xsl:with-param>
        </xsl:apply-templates>
        
        <xsl:text>;&#xa;</xsl:text>
    </xsl:template>
      
    <!-- creates names of EdgeClass out of role names or names of adjacent VertexClasses
        parameter association: Specifies the association for which to create a name.
        parameter fromAggregateAttribute: Specifies the aggregation attribute for the association's from side
        parameter toAggregateAttribute: Specifies the aggregation attribute for the association's to side
        parameter toVertexClass: Specifies the name of the "to"-VertexClass
        parameter toRoleName: Specifies the name of the "to"-role --> 
    <xsl:template name="edgeClassName">
        <xsl:param name="association"/>
        
        <!-- store EdgeClass's package prefix -->
        <xsl:variable name="packagePrefix" select="myfunctions:getPackagePrefix($association, $schemaPackage)"/>
       
        <!-- store XPaths to aggregate attribute of source VertexClass -->
        <xsl:variable name="fromAggregateAttribute" select="$schemaPackage//packagedElement[$association/ownedEnd/type/@xmi:idref = @xmi:id
            or ownedAttribute/@xmi:id = $association/memberEnd/@xmi:idref]/ownedAttribute[@association = $association/@xmi:id 
            and contains(@xmi:id, 'src')]/@aggregation 
            union $association/ownedEnd[contains(@xmi:id, 'src')]/@aggregation"/>
        
        <!-- store XPaths to aggregate attribute of target VertexClass -->
        <xsl:variable name="toAggregateAttribute" select="$schemaPackage//packagedElement[$association/ownedEnd/type/@xmi:idref = @xmi:id
            or ownedAttribute/@xmi:id = $association/memberEnd/@xmi:idref]/ownedAttribute[@association = $association/@xmi:id 
            and contains(@xmi:id, 'dst')]/@aggregation 
            union $association/ownedEnd[contains(@xmi:id, 'dst')]/@aggregation"/>
        
        <!-- store XPaths to aggregate attribute of both source and target VertexClasses-->
        <xsl:variable name="aggregateAttributes" select="$fromAggregateAttribute union $toAggregateAttribute"/>    
        
        <!-- "from" VertexClass 
            The expression says that either the id of a type of an ownedEnd of the currently
            processed EdgeClass with an id containing 'dst' must be equal to the id of the
            looked-for packagedElement, or that the id of an ownedAttribute of the looked-for
            packagedElement must contain 'dst' and be equal to the id of a memberEnd of the
            currently processed EdgeClass. -->
        <xsl:variable name="fromVertexClass" select="$schemaPackage//packagedElement[$association/ownedEnd[contains(@xmi:id, 'src')]/type/@xmi:idref = @xmi:id
            or $schemaPackage//packagedElement/ownedAttribute[contains(@xmi:id, 'src') and @xmi:id = $association/memberEnd/@xmi:idref]/type/@xmi:idref = @xmi:id]"/>
        
        <!-- name of "from" VertexClass -->
        <xsl:variable name="fromVertexClassName" select="if ($autoCorrect='yes')
            then myfunctions:correctQualifiedName($fromVertexClass/@name) else $fromVertexClass/@name"/>
        
        <!-- "to" VertexClass 
            The expression says that either the id of a type of an ownedEnd of the currently
            processed EdgeClass with an id containing 'dst' must be equal to the id of the
            looked-for packagedElement, or that the id of an ownedAttribute of the looked-for
            packagedElement must contain 'dst' and be equal to the id of a memberEnd of the
            currently processed EdgeClass. -->
        <xsl:variable name="toVertexClass" select="$schemaPackage//packagedElement[$association/ownedEnd[contains(@xmi:id, 'dst')]/type/@xmi:idref = @xmi:id
            or $schemaPackage//packagedElement/ownedAttribute[contains(@xmi:id, 'dst') and @xmi:id = $association/memberEnd/@xmi:idref]/type/@xmi:idref = @xmi:id]"/>
        
        <!-- name of "to" VertexClass -->
        <xsl:variable name="toVertexClassName" select="if ($autoCorrect='yes')
            then myfunctions:correctQualifiedName($toVertexClass/@name) else $toVertexClass/@name"/>
        
        <!-- name of "from" role
            The expression says that the name attribute of the ownedAttribute corresponding to the association source (non-navigable end) shall be concatenated with the name
            attribute of the ownedEnd corresponding to the association source. Since one of these strings is empty, the result is the "from"-rolename. -->
        <xsl:variable name="fromRoleName" select="concat($schemaPackage//ownedAttribute[contains(@xmi:id, 'src') and @xmi:id = $association/memberEnd/@xmi:idref]/@name,
            $association/ownedEnd[contains(@xmi:id, 'src')]/@name)"/>
        
        <!-- name of "to" role
            The expression says that the name attribute of the ownedAttribute corresponding to the association destination (non-navigable end) shall be concatenated with the name
            attribute of the ownedEnd corresponding to the association destination. Since one of these strings is empty, the result is the "to"-rolename. -->
        <xsl:variable name="toRoleName" select="concat($schemaPackage//ownedAttribute[contains(@xmi:id, 'dst') and @xmi:id = $association/memberEnd/@xmi:idref]/@name,
            $association/ownedEnd[contains(@xmi:id, 'dst')]/@name)"/>
               
        <!-- create package prefix -->
        <xsl:if test="$packagePrefix != ''">
            <xsl:if test="$autoCorrect = 'yes'">
                <xsl:value-of select="myfunctions:correctPackagePrefix($packagePrefix)"/>
            </xsl:if>
            <xsl:if test="$autoCorrect = 'no'">
                <xsl:value-of select="$packagePrefix"/>
            </xsl:if>
        </xsl:if>        
        
        <xsl:if test="$autoCorrect = 'yes'">
            <!-- if the association has no name -->
            <xsl:if test="empty($association/@name) or $association/@name = ''">
                <!-- if $extendedEdgeClassNames = "yes", then prepend, if existing,  fromRoleName or, alternatively, fromVertexClassName-->
                <xsl:if test="$extendedEdgeClassNames = 'yes'">
                    <xsl:if test="$fromRoleName = ''">
                        <xsl:value-of select="$fromVertexClassName"/>
                    </xsl:if>
                    <xsl:if test="$fromRoleName != ''">
                        <xsl:value-of select="myfunctions:firstToUpperCase($fromRoleName)"/>
                    </xsl:if>
                </xsl:if>     
                
                <!-- if the association is actually an aggregation or composition -->
                <xsl:if test="some $aggr in $aggregateAttributes satisfies ($aggr = 'shared' or $aggr = 'composite')">
                    <!-- if the composite is on the "to" side -->
                    <xsl:if test="$toAggregateAttribute != 'none'">
                        <xsl:text>IsPartOf</xsl:text>
                    </xsl:if>
                    <!-- if the component is on the "to" side -->
                    <xsl:if test="$toAggregateAttribute = 'none'">
                        <xsl:text>Contains</xsl:text>
                    </xsl:if>
                </xsl:if>
                <!-- if the association is no aggregation or composition -->
                <xsl:if test="every $aggr in $aggregateAttributes satisfies $aggr = 'none'">
                    <xsl:text>LinksTo</xsl:text>
                </xsl:if>
                
                <!-- append, if existing,  toRoleName or, alternatively, toVertexClassName-->
                <xsl:if test="$toRoleName = ''">
                    <xsl:value-of select="$toVertexClassName"/>
                </xsl:if>
                <xsl:if test="$toRoleName != ''">
                    <xsl:value-of select="myfunctions:firstToUpperCase($toRoleName)"/>
                </xsl:if>
            </xsl:if>
                            
            <!-- if the association has a name -->
            <xsl:if test="exists($association/@name) and $association/@name != ''">
                <xsl:value-of select="myfunctions:correctQualifiedNameCase(myfunctions:removeSpaces($association/@name))"/>
            </xsl:if>
            
            <!-- TODO: does not work for navigable ends. Furthermore, ids are generated even if aggregations are on opposite sides -->
            <!-- if there are two or more associations
                with the same name or without a name (1st line),
                with the same "from"-VertexClass (2nd to 5th line),
                with the same "to" VertexClass (6th to 9th line),
                with aggregation type ('shared' or 'composite') (10th to 17th line) or 'none' (18th to 21th line),
                append an unambiguous id to the EdgeClass name -->
            <!--           <xsl:if test="count($schemaPackage//packagedElement[(@name = current()/@name or empty(@name) and empty(current()/@name))
                and ($fromVertexClass/@xmi:id
                        = myfunctions:getAssociation(.)/ownedEnd[contains(@xmi:id, 'src')]/type/@xmi:idref         
                    or $toVertexClass/@xmi:id
                        = myfunctions:getAssociation(.)/ownedEnd[contains(@xmi:id, 'dst')]/type/@xmi:idref)]) > 1">
                    and ((exists($fromVertexClass/ownedAttribute[@aggregation != 'none' and type/@xmi:idref = $toVertexClass/@xmi:id] 
                            union myfunctions:getAssociation(.)/ownedEnd[@aggregation != 'none' and type/@xmi:idref = $toVertexClass/@xmi:id])
                        and exists($toVertexClass/ownedAttribute[@aggregation = 'none' and type/@xmi:idref = $fromVertexClass/@xmi:id] 
                            union myfunctions:getAssociation(.)/ownedEnd[@aggregation = 'none' and type/@xmi:idref = $fromVertexClass/@xmi:id]))
                    or (exists($fromVertexClass/ownedAttribute[@aggregation = 'none' and type/@xmi:idref = $toVertexClass/@xmi:id] 
                            union myfunctions:getAssociation(.)/ownedEnd[@aggregation = 'none' and type/@xmi:idref = $toVertexClass/@xmi:id])
                        and exists($toVertexClass/ownedAttribute[@aggregation != 'none' and type/@xmi:idref = $fromVertexClass/@xmi:id] 
                            union myfunctions:getAssociation(.)/ownedEnd[@aggregation != 'none' and type/@xmi:idref = $fromVertexClass/@xmi:id]))
                    or ((every $aggr in $aggregateAttributes satisfies $aggr = 'none')
                    and (every $aggr2 in (.[myfunctions:getAssociation(.)/ownedEnd/type/@xmi:idref = @xmi:id
                            or ownedAttribute/@xmi:id = myfunctions:getAssociation(.)/memberEnd/@xmi:idref]/ownedAttribute[@association = myfunctions:getAssociation(.)/@xmi:id]/@aggregation 
                        union myfunctions:getAssociation(.)/ownedEnd/@aggregation) satisfies $aggr2 = 'none')))]) > 1"> 
                    </xsl:if>
 -->
            <xsl:if  test="$appendEdgeIds = 'yes'">
                <xsl:text>_</xsl:text>
                <xsl:value-of select="generate-id($association)"/>
            </xsl:if> 
        </xsl:if>
        <xsl:if test="$autoCorrect = 'no'">
            <xsl:value-of select="$association/@name"/>
        </xsl:if>    
    </xsl:template>
    
    <!-- creates role names
        parameter rolename: specifies the rolename which shall be created
        parameter association: specifies the association (class) for which a role name shall be created -->
    <xsl:template name="role">
        <xsl:param name="rolename"/>
        
        <xsl:if test="$rolename != ''">
            <xsl:text> role </xsl:text>
            
            <xsl:if test="$autoCorrect = 'yes'">
                <xsl:value-of select="myfunctions:removeReservedWordsConflicts(myfunctions:removeSpaces($rolename))"/>
            </xsl:if>
            <xsl:if test="$autoCorrect = 'no'">
                <xsl:value-of select="$rolename"/>
            </xsl:if>
                      
        </xsl:if>
    </xsl:template>
    
    <!-- created redefinitions -->
    <xsl:template match="redefinedProperty" mode="redefines">
        <xsl:choose>
            <xsl:when test="position() = 1">
                <xsl:text> redefines </xsl:text>
            </xsl:when>
            <xsl:otherwise>
                <xsl:text>, </xsl:text>
            </xsl:otherwise>
        </xsl:choose>
        
        <xsl:value-of select="$schemaPackage//packagedElement/ownedEnd[@xmi:id = current()/@xmi:idref]/@name union $schemaPackage//packagedElement/ownedAttribute[@xmi:id = current()/@xmi:idref]/@name"/>        
    </xsl:template>
    
    <!-- creates multiplicities
        parameter side: specifies whether the source ('src') or destination ('dst') 
            vertex class role name shall be created
        parameter association: specifies the association (class) for which multiplicities shall be created -->
    <xsl:template name="multiplicity">
        <xsl:param name="side"/>
        <xsl:param name="association"/>
        
        <xsl:choose>
            <!-- if tool is EA, then data from xmi:Extension is used -->
            <xsl:when test="$tool='ea'">
                <xsl:text> (</xsl:text>
                
                <!-- create the multiplicity's lower value at the source vertex class -->
                <xsl:if test="$side='src'">
                    <!-- store the path to the lower multiplicity value
                        The connector's idref must either match the association id or the
                            conID of the element (association class) whose idref matches the
                            association id -->
                    <xsl:variable name="pathToValue" select="/xmi:XMI/xmi:Extension/connectors/connector[@xmi:idref = $association/@xmi:id or @xmi:idref =
                        /xmi:XMI/xmi:Extension/elements/element[@xmi:idref = $association/@xmi:id]/extendedProperties/@conID]/source/type/@multiplicity"/>
                    
                    <xsl:if test="$autoCorrect='yes' and empty($pathToValue)">
                        <xsl:text>1</xsl:text>   
                    </xsl:if>
                    <xsl:if test="$autoCorrect='no' or exists($pathToValue)">
                        <!-- if $pathToValue contains '..', the number in front of '..' is the multiplicity's
                            lower value, else if $pathToValue = '*', the lower value is '0', else it is $pathToValue --> 
                        <xsl:variable name="value" 
                            select="if (contains($pathToValue, '..'))
                                then substring-before($pathToValue, '..')
                                else if ($pathToValue = '*') then '0' else $pathToValue"/>
                        <xsl:value-of select="$value"/> 
                    </xsl:if>
                </xsl:if>
                
                <!-- create the multiplicity's lower value at the target vertex class -->
                <xsl:if test="$side='dst'">
                    <xsl:variable name="pathToValue" select="/xmi:XMI/xmi:Extension/connectors/connector[@xmi:idref = $association/@xmi:id or @xmi:idref = 
                        /xmi:XMI/xmi:Extension/elements/element[@xmi:idref = $association/@xmi:id]/extendedProperties/@conID]/target/type/@multiplicity"/>
                    
                    <xsl:if test="$autoCorrect='yes' and empty($pathToValue)">
                        <xsl:text>1</xsl:text>   
                    </xsl:if>
                    <xsl:if test="$autoCorrect='no' or exists($pathToValue)">
                        <xsl:variable name="value" 
                            select="if (contains($pathToValue, '..'))
                                then substring-before($pathToValue, '..') 
                                    else if ($pathToValue = '*') then '0' else $pathToValue"/>
                        <xsl:value-of select="$value"/>
                    </xsl:if>
                </xsl:if>
                
                <xsl:text>,</xsl:text>
                
                <!-- create the multiplicity's upper value at the source vertex class -->
                <xsl:if test="$side='src'">
                    <xsl:variable name="pathToValue" select="/xmi:XMI/xmi:Extension/connectors/connector[@xmi:idref = $association/@xmi:id or @xmi:idref =
                        /xmi:XMI/xmi:Extension/elements/element[@xmi:idref = $association/@xmi:id]/extendedProperties/@conID]/source/type/@multiplicity"/>
                    
                    <xsl:if test="$autoCorrect='yes' and empty($pathToValue)">
                        <xsl:text>1</xsl:text>   
                    </xsl:if>
                    <xsl:if test="$autoCorrect='no' or exists($pathToValue)">
                        <xsl:variable name="value" 
                            select="if (contains($pathToValue, '..'))
                                then substring-after($pathToValue, '..') else $pathToValue"/>
                        <xsl:value-of select="$value"/>
                    </xsl:if>
                </xsl:if>
                
                <!-- create the multiplicity's upper value at the target vertex class -->
                <xsl:if test="$side='dst'">    
                    <xsl:variable name="pathToValue" select="/xmi:XMI/xmi:Extension/connectors/connector[@xmi:idref = $association/@xmi:id or @xmi:idref =
                        /xmi:XMI/xmi:Extension/elements/element[@xmi:idref = $association/@xmi:id]/extendedProperties/@conID]/target/type/@multiplicity"/>
                    
                    <xsl:if test="$autoCorrect='yes' and empty($pathToValue)">
                        <xsl:text>1</xsl:text>   
                    </xsl:if>
                    <xsl:if test="$autoCorrect='no' or exists($pathToValue)">
                        <xsl:variable name="value" 
                            select="if (contains($pathToValue, '..'))
                                then substring-after($pathToValue, '..') else $pathToValue"/>
                        <xsl:value-of select="$value"/>  
                    </xsl:if>
                </xsl:if>
                
                <xsl:text>)</xsl:text>
            </xsl:when>
            <xsl:otherwise>
                <xsl:text> (</xsl:text>
                
                <!-- create the multiplicity's lower value 
                    The variables contain the possible XPaths to the lower value: "lowerValueNav" if the
                        association end indicated by $side is navigable, "lowerValueNonNav" if the association end is
                        not navigable -->
                <xsl:variable name="lowerValueNav" select="$schemaPackage//ownedAttribute[contains(@xmi:id, $side) and @xmi:id = $association/memberEnd/@xmi:idref]/lowerValue/@value"/>
                <xsl:variable name="lowerValueNonNav" select="$association/ownedEnd[contains(@xmi:id, $side)]/lowerValue/@value"/>
                <xsl:if test="$autoCorrect = 'yes'">
                    <xsl:if test="empty($lowerValueNav) and empty($lowerValueNonNav)">
                        <xsl:text>1</xsl:text>
                    </xsl:if>
                    <xsl:if test="exists($lowerValueNav) or exists($lowerValueNonNav)">
                        <xsl:value-of select="$lowerValueNav"/>
                        <xsl:value-of select="$lowerValueNonNav"/>
                    </xsl:if>
                </xsl:if>
                <xsl:if test="$autoCorrect = 'no'">
                    <xsl:value-of select="$lowerValueNav"/>
                    <xsl:value-of select="$lowerValueNonNav"/>
                </xsl:if>
                
                <xsl:text>,</xsl:text>
                
                <!-- create the multiplicity's upper value
                    The variables contain the possible XPaths to the upper value: "upperValueNav" if the
                        association end indicated by $side is navigable, "upperValueNonNav" if the association end is
                        not navigable -->
                <xsl:variable name="upperValueNav" select="$schemaPackage//ownedAttribute[contains(@xmi:id, $side) and @xmi:id = $association/memberEnd/@xmi:idref]/upperValue/@value"/>
                <xsl:variable name="upperValueNonNav" select="$association/ownedEnd[contains(@xmi:id, $side)]/upperValue/@value"/>
                <xsl:if test="$autoCorrect = 'yes'">
                    <xsl:if test="empty($upperValueNav) and empty($upperValueNonNav)">
                        <xsl:text>1</xsl:text>
                    </xsl:if>
                    <xsl:if test="exists($upperValueNav) or exists($upperValueNonNav)">
                        <xsl:if test="$upperValueNav = '-1' or $upperValueNonNav = '-1'">
                            <xsl:text>*</xsl:text>
                        </xsl:if>
                        <xsl:if test="$upperValueNav != '-1' or $upperValueNonNav != '-1'">
                            <xsl:value-of select="$upperValueNav"/>
                            <xsl:value-of select="$upperValueNonNav"/>
                        </xsl:if>
                    </xsl:if>
                </xsl:if>
                    
                <xsl:if test="$autoCorrect = 'no'">
                    <xsl:if test="$upperValueNav = '-1' or $upperValueNonNav = '-1'">
                        <xsl:text>*</xsl:text>
                    </xsl:if>
                    <xsl:if test="$upperValueNav != '-1' or $upperValueNonNav != '-1'">
                        <xsl:value-of select="$upperValueNav"/>
                        <xsl:value-of select="$upperValueNonNav"/>
                    </xsl:if>
                </xsl:if>
                
                <xsl:text>)</xsl:text>
            </xsl:otherwise>
        </xsl:choose>   
    </xsl:template>
    
    <!-- creates generalization hierarchy -->
    <xsl:template name="generalization">
        <xsl:param name="elements"/>
        
        <xsl:for-each select="$elements">
            
            <xsl:choose>
                <xsl:when test="position() = 1">
                    <xsl:text>: </xsl:text>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:text>, </xsl:text>
                </xsl:otherwise>
            </xsl:choose>
            
            <xsl:call-template name="edgeClassName">
                <xsl:with-param name="association" select="current()"/>
            </xsl:call-template>                                
            
        </xsl:for-each>
        
    </xsl:template>
    
    <!-- creates enum constants, record components and attributes of graph element classes -->
    <xsl:template match="ownedAttribute[not(@association)]">
        <xsl:param name="caller"/>
        
        <!-- write opening parenthesis before first attribute -->
        <xsl:if test="position() = 1">
            <xsl:if test="$caller='attributedElementClass'">
                <xsl:text> {</xsl:text>
            </xsl:if>
            <xsl:if test="$caller='enum' or $caller='record'">
                <xsl:text> (</xsl:text>
            </xsl:if>
        </xsl:if>
        <xsl:if test="position() > 1">
            <xsl:text>, </xsl:text>
        </xsl:if>
        
        <!-- write the attribute's name; if autoCorrect is on and it is an enum constant, it is converted to uppercase -->
        <xsl:if test="$autoCorrect = 'yes'">
            <xsl:if test="$caller = 'enum'">
                <xsl:value-of select="upper-case(@name)"/>
            </xsl:if>
            <xsl:if test="$caller != 'enum'">
                <xsl:value-of select="@name"/>
            </xsl:if>
        </xsl:if>
        <xsl:if test="$autoCorrect = 'no'">
            <xsl:value-of select="@name"/>
        </xsl:if>        
        
        <xsl:if test="$caller!='enum'">
            <xsl:text>: </xsl:text>
            <!-- if tool is EA and Domain is primitive, then data from xmi:Extension is used -->
            <xsl:if test="$tool = 'ea' and exists(index-of($reservedWords, /xmi:XMI/xmi:Extension/elements/element/attributes/attribute[@xmi:idref = current()/@xmi:id]/properties/@type))">
                <xsl:value-of select="/xmi:XMI/xmi:Extension/elements/element/attributes/attribute[@xmi:idref = current()/@xmi:id]/properties/@type"/>
            </xsl:if>
            <xsl:if test="$tool != 'ea' or empty(index-of($reservedWords, /xmi:XMI/xmi:Extension/elements/element/attributes/attribute[@xmi:idref = current()/@xmi:id]/properties/@type))">
                <xsl:if test="$autoCorrect = 'yes'">
                    <xsl:value-of select="myfunctions:correctQualifiedName(myfunctions:getQualifiedName($schemaPackage//packagedElement[@name = /xmi:XMI/xmi:Extension/elements/element/attributes/attribute[@xmi:idref = current()/@xmi:id]/properties/@type], $schemaPackage))"/>
                </xsl:if>
                <xsl:if test="$autoCorrect = 'no'">
                    <xsl:value-of select="myfunctions:getQualifiedName($schemaPackage//packagedElement[@name = /xmi:XMI/xmi:Extension/elements/element/attributes/attribute[@xmi:idref = current()/@xmi:id]/properties/@type], $schemaPackage)"/>
                </xsl:if>
            </xsl:if>   
        </xsl:if>
        
        <!-- write closing parenthesis after last attribute -->
        <xsl:if test="position()=last()">
            <xsl:if test="$caller='attributedElementClass'">
                <xsl:text>}</xsl:text>
            </xsl:if>
            <xsl:if test="$caller='enum' or $caller='record'">
                <xsl:text>)</xsl:text> 
            </xsl:if>
        </xsl:if>
    </xsl:template>
    
    <xsl:template name="packagePrefix">
        
        
    </xsl:template>
    
    <!-- inserts '// ' after line breaks in given string and returns the resulting string -->
    <xsl:function name="myfunctions:adaptNotes" as="xs:string">
        <xsl:param name="inputString"/>
        
        <xsl:sequence select="replace($inputString, '&#xa;', '&#xa;// ')"/>
    </xsl:function>
    
    <!--
        <xsl:function name="myfunctions:convertSpacesToCamelCase" as="xs:string">
        <xsl:param name="inputString"/>
        
        <xsl:sequence select="replace($inputString, ' (.)', upper-case('$1'))"/>     
        </xsl:function>
    -->
 
    <!-- performs autoCorrect measures on given package prefix
        parameter inputString: package prefix which shall be corrected
        returns: corrected $inputString -->
    <xsl:function name="myfunctions:correctPackagePrefix" as="xs:string">
        <xsl:param name="inputString" as="xs:string"/>
        
        <xsl:sequence select="myfunctions:removeReservedWordsConflicts(lower-case(myfunctions:removeSpaces($inputString)))"/>
    </xsl:function>
    
    <!-- performs autoCorrect measures on given qualified name
        parameter inputString: qualified name which shall be corrected
        returns: corrected $inputString -->
    <xsl:function name="myfunctions:correctQualifiedName" as="xs:string">
        <xsl:param name="inputString" as="xs:string"/>
        
        <xsl:sequence select="myfunctions:removeReservedWordsConflicts(myfunctions:correctQualifiedNameCase(myfunctions:removeSpaces($inputString)))"/>
    </xsl:function>
    
    <!-- corrects the case of the qualified name, i.e. package prefix all lower-case and the name
        of the AttributedElementClass with the first character upper case -->
    <xsl:function name="myfunctions:correctQualifiedNameCase">
        <xsl:param name="inputString" as="xs:string"/>
        
        <xsl:variable name="tokenization" select="tokenize($inputString, '\.')" as="xs:string*"/>
        <xsl:variable name="className" select="$tokenization[count($tokenization)]" as="xs:string"/>
        <xsl:variable name="packagePrefix" select="string-join(remove($tokenization, count($tokenization)), '.')" as="xs:string"/>
        <xsl:sequence select="if ($packagePrefix = '')
            then myfunctions:firstToUpperCase($className)
            else concat(lower-case($packagePrefix), '.', myfunctions:firstToUpperCase($className))"/>
    </xsl:function>
    
    <!-- converts the first character of the given string to uppercase
        parameter inputString: string whose first character is to be converted
        returns: $inputString with first character in uppercase -->
    <xsl:function name="myfunctions:firstToUpperCase" as="xs:string">
        <xsl:param name="inputString"/>
        
        <xsl:variable name="headOfInputString" select="substring($inputString, 1, 1)"/>
        <xsl:variable name="tailOfInputString" select="substring($inputString, 2)"/>
        <xsl:sequence select="concat(upper-case($headOfInputString), $tailOfInputString)"/>
    </xsl:function>              
    
    <!-- checks if node given as parameter is subclass of one or more associations or association 
            classes and returns these superclasses, otherwise an empty sequence is returned
        parameter class: node to be checked
        returns: a sequence of all associations and association classes which are superclasses of $node -->
    <xsl:function name="myfunctions:getAssociation">
        <xsl:param name="class"/>
        
        <xsl:sequence select="
            if (every $node in $class satisfies $node/@xmi:type = 'uml:AssociationClass' or $node/@xmi:type = 'uml:Association') 
                then $class
                else if (empty($class/generalization))
                    then () 
                    else myfunctions:getAssociation(root($class[1])//xmi:XMI/uml:Model//packagedElement[@xmi:id = $class/generalization/@general])"/>            
    </xsl:function>
    
    <!-- for the given class, this function returns the next class in the given class' generalization hierarchy which is connected to an association and has a
        superclass whose name is given in $classesToEdgeClasses -->
    <xsl:function name="myfunctions:getClassToEdgeClass">
        <xsl:param name="class"/>
        
        <xsl:sequence select="
            if (some $node in $class satisfies (myfunctions:isClassToEdgeClass($node)) 
                    and (exists(root($class[1])/xmi:XMI/uml:Model//packagedElement[
                            ownedEnd/@name = 'source']/ownedEnd/type[@xmi:idref = $node/@xmi:id])))
                then $class
                else if (empty($class/generalization)) 
                    then ()
                    else myfunctions:getClassToEdgeClass(root($class[1])/xmi:XMI/uml:Model//packagedElement[@xmi:id = $class/generalization/@general])"/>
    </xsl:function>
    
    <!-- returns all the superclasses of the given class, including the class itself
        parameter class: the class whose superclasses shall be returned
        returns: the superclasses of the given class, including the class itself -->
    <xsl:function name="myfunctions:getGeneralizationHierarchy">
        <xsl:param name="class"/>
        
        <xsl:sequence select="
            if (empty($class/generalization))
            then $class
            else ($class, myfunctions:getGeneralizationHierarchy(root($class[1])/xmi:XMI/uml:Model//packagedElement[@xmi:id = $class/generalization/@general]))"/>
    </xsl:function>
    
    <!-- constructs the package prefix of the given element -->
    <xsl:function name="myfunctions:getPackagePrefix">
        <xsl:param name="element"/>
        <xsl:param name="schemaPackage"/>
        
        <xsl:if test="$element/@xmi:type = 'uml:AssociationClass'">
            <xsl:variable name="parentPackage" select="
                $schemaPackage//packagedElement[@xmi:id = root($element)/xmi:XMI/xmi:Extension/elements/element[@xmi:idref = $element/@xmi:id]/model/@package]"/>
            
            <xsl:sequence select="if ($parentPackage != $schemaPackage)
                then concat(myfunctions:getPackagePrefix($parentPackage, $schemaPackage), $parentPackage/@name, '.') 
                else ''"/>
        </xsl:if>
        <xsl:if test="$element/@xmi:type != 'uml:AssociationClass'">
            <xsl:sequence select="
                if ($element/parent::packagedElement != $schemaPackage)
                    then concat(myfunctions:getPackagePrefix($element/parent::packagedElement, $schemaPackage), $element/parent::packagedElement/@name, '.')
                    else ''"/>
        </xsl:if>
    </xsl:function>
    
    <!-- constructs the qualified name of the given element -->
    <xsl:function name="myfunctions:getQualifiedName">
        <xsl:param name="element"/>
        <xsl:param name="schemaPackage"/>
        
        <xsl:variable name="packagePrefix" select="myfunctions:getPackagePrefix($element, $schemaPackage)"/>
        <xsl:sequence select="concat($packagePrefix, $element/@name)"/>            
    </xsl:function>
    
    <!-- checks if given class shall be converted to an EdgeClass; this is the case if its name is contained in parameter $classToEdgeClass or it is a
            subclass of one of these classes
        parameter class: class to be checked if it shall be converted to an EdgeClass
        returns: true if given class shall be converted to an EdgeClass, else false -->
    <xsl:function name="myfunctions:isClassToEdgeClass" as="xs:boolean">
        <xsl:param name="class"/>
        
        <xsl:sequence select="
            if (some $node in myfunctions:getGeneralizationHierarchy($class) satisfies exists(index-of($classToEdgeClass, $node/@name)))
                then true()
                else false()"/>        
    </xsl:function>
    
    <!-- checks if given association is connected to a class which shall be converted to an EdgeClass
        parameter association: association to be checked if connected class shall be converted to an EdgeClass
        returns: true if a connected class shall be converted to an EdgeClass, else false -->
    <xsl:function name="myfunctions:isClassToEdgeClassAssociation" as="xs:boolean">
        <xsl:param name="association"/>
        
        <xsl:sequence select="
            if (myfunctions:isClassToEdgeClass(root($association[1])/xmi:XMI/uml:Model//packagedElement[ownedAttribute/@association = $association/@xmi:id
                    or @xmi:id = root($association[1])/xmi:XMI/uml:Model//packagedElement[@xmi:id = $association/@xmi:id]/ownedEnd/type/@xmi:idref]))
                then true()
                else false()"/>
    </xsl:function>                           
    
    <!-- checks whether the given string is equal to a reserved word of JGraLab; if true, "'" is prepended and
            the resulting string returned; if false, the input is returned unchanged
        parameter inputString: string to be checked against reserved words 
        returns: $inputString if it is not a reserved word, else "'" + $inputString -->
    <xsl:function name="myfunctions:removeReservedWordsConflicts" as="xs:string">
        <xsl:param name="inputString" as="xs:string"/>
        
        <xsl:sequence select="
            if (empty(index-of($reservedWords, $inputString)))
                then $inputString
                else concat('''', $inputString)"/>
    </xsl:function>
    
    <!-- removes spaces contained in given string and returns the result
        parameter inputString: string whose spaces are to be removed
        returns: $inputString with spaces removed -->
    <xsl:function name="myfunctions:removeSpaces" as="xs:string">
        <xsl:param name="inputString"/>
        
        <xsl:sequence select="replace($inputString, ' ', '')"/>     
    </xsl:function>
   
</xsl:stylesheet>