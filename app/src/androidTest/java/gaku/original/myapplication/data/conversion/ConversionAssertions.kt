package gaku.original.myapplication.data.conversion

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor

/**
 * Fails when [sample] has a property that is null or equal to its default value.
 *
 * A round trip test can only detect a dropped field when the sample sets it to a value
 * that the reader would not produce by itself. So when a new property is added to a data class,
 * this fails first and asks you to set it in the sample.
 */
fun assertFullyPopulated(sample: Any) {
    val kClass = sample::class
    val constructor = kClass.primaryConstructor ?: return
    val sampleArguments = constructor.parameters.associateWith { kClass.propertyValue(sample, it.name!!) }

    constructor.parameters.forEach { parameter ->
        val name = parameter.name!!
        val value = sampleArguments.getValue(parameter)
        if (value == null || (value is Collection<*> && value.isEmpty())) {
            fail("${kClass.simpleName}.$name is not set in the test sample. Set a non-null value.")
        }
        if (!parameter.isOptional) return@forEach
        /* Take every other argument from the sample, so this also works for classes with required parameters. */
        val defaultInstance = constructor.callBy(sampleArguments - parameter)
        if (value == kClass.propertyValue(defaultInstance, name)) {
            fail(
                "${kClass.simpleName}.$name in the test sample equals its default value ($value). " +
                        "Use a different value, otherwise a dropped field cannot be detected."
            )
        }
    }
}

/**
 * Compares [expected] and [actual] property by property, which gives a clearer message than
 * a whole data class comparison.
 *
 * [notStored] lists properties that are intentionally not saved.
 * It must contain existing property names only.
 */
fun <T : Any> assertSameProperties(expected: T, actual: T, notStored: Set<String> = emptySet()) {
    val kClass = expected::class
    val names = kClass.primaryConstructor?.parameters?.map { it.name!! } ?: return
    val unknown = notStored - names.toSet()
    assertTrue("Unknown properties in notStored: $unknown", unknown.isEmpty())

    names.filterNot { it in notStored }.forEach { name ->
        assertEquals(
            "${kClass.simpleName}.$name was not converted correctly",
            kClass.propertyValue(expected, name),
            kClass.propertyValue(actual, name)
        )
    }
}

/** Fails when a subtype of [sealedClass] has no sample. Detects newly added subtypes. */
fun assertCoversAllSubclasses(sealedClass: KClass<*>, samples: List<Any>) {
    val missing = sealedClass.sealedSubclasses.toSet() - samples.map { it::class }.toSet()
    assertTrue(
        "No test sample for ${missing.map { it.simpleName }} of ${sealedClass.simpleName}",
        missing.isEmpty()
    )
}

private fun KClass<*>.propertyValue(instance: Any, name: String): Any? =
    memberProperties.first { it.name == name }.getter.call(instance)
