<#-- Hibernate Tools 6.x Compatible Template -->
<#-- Available objects: pojo, clazz -->
${pojo.getPackageDeclaration()}

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "${clazz.table.name}")
public class ${pojo.getDeclarationName()} implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

<#list pojo.getAllPropertiesIterator() as property>
    <#assign propertyName = property.name>
    <#assign javaType = pojo.getJavaTypeName(property, true)>
    <#assign valueTypeName = property.value.class.simpleName>
    <#assign isId = pojo.hasIdentifierProperty() && pojo.getIdentifierProperty().name == propertyName>

    <#if isId>
    @Id
    <#if ["Long", "Integer", "Short", "Byte", "long", "int", "short", "byte"]?seq_contains(javaType)>
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    </#if>
    <#if property.value.columnIterator?? && property.value.columnIterator.hasNext()>
        <#assign column = property.value.columnIterator.next()>
    @Column(name = "${column.name}")
    </#if>
    private ${javaType} ${propertyName};
    <#elseif valueTypeName == "ManyToOne">
    @ManyToOne(fetch = FetchType.LAZY)
    <#if property.value.columnIterator?? && property.value.columnIterator.hasNext()>
        <#assign column = property.value.columnIterator.next()>
    @JoinColumn(name = "${column.name}"<#if !column.nullable>, nullable = false</#if>)
    </#if>
    private ${javaType} ${propertyName};
    <#elseif valueTypeName == "Set" || valueTypeName == "Bag" || valueTypeName == "List">
    <#-- Inverse OneToMany side — skipped intentionally -->
    <#else>
    <#if property.value.columnIterator?? && property.value.columnIterator.hasNext()>
        <#assign column = property.value.columnIterator.next()>
    @Column(name = "${column.name}"<#if !column.nullable>, nullable = false</#if>)
    </#if>
    private ${javaType} ${propertyName};
    </#if>
</#list>
}
