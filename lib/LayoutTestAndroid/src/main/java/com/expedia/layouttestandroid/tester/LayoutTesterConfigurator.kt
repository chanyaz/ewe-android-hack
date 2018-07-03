package com.expedia.layouttestandroid.tester

import com.expedia.layouttestandroid.tester.predefined.EmptyViewGroupTester
import com.expedia.layouttestandroid.tester.predefined.NestedHierarchyTester
import com.expedia.layouttestandroid.tester.predefined.ViewOverlapTester
import com.expedia.layouttestandroid.tester.predefined.ViewWithInSuperviewTester

class LayoutTesterConfigurator {
    val viewOverlapTester = ViewOverlapTester()
    val viewWithInSuperviewTester = ViewWithInSuperviewTester()
    val nestedHierarchyTester = NestedHierarchyTester()
    val emptyViewGroupTester = EmptyViewGroupTester()

    private val testers = hashSetOf(viewOverlapTester, viewWithInSuperviewTester, nestedHierarchyTester, emptyViewGroupTester)

    fun disableViewOverlapTester() = testers.remove(viewOverlapTester)

    fun disableViewWithInSuperviewTester() = testers.remove(viewWithInSuperviewTester)

    fun disableNestedHierarchyTester() = testers.remove(nestedHierarchyTester)

    fun disableEmptyViewGroupTester() = testers.remove(emptyViewGroupTester)

    fun addTester(layoutTester: LayoutTester) = testers.add(layoutTester)

    fun getAllTesters(): Set<LayoutTester> = testers
}
