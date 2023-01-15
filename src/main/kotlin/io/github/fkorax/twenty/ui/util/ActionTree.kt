/*
 * Copyright © 2022, 2023  Franchesko Korako
 *
 * This file is part of 20ty.
 *
 * 20ty is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * 20ty is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with 20ty.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.fkorax.twenty.ui.util

import io.github.fkorax.twenty.util.Either
import javax.swing.Action

@JvmInline
value class ActionTree<T : Action>(val root: Node<T>) {

    sealed class Node<T : Action> {
        data class Leaf<T : Action>(val action: T) : Node<T>()

        data class Branch<T : Action>(val text: String, val nodes: List<Node<T>>) : Node<T>() {
            constructor(text: String, vararg nodes: Node<T>) : this(text, arrayListOf(*nodes))
        }
    }

    /**
     * A utility interface for processing the [Node]s in an [ActionTree].
     * Implementing types simply need to override the two methods [processLeaf] and [processBranch];
     * the interface offers a default implementation of [processNode].
     */
    interface NodeProcessor<T : Action> {

        fun processLeaf(leaf: Node.Leaf<T>)

        /**
         * In the default implementation, this function is called by
         * [processNode] to process the branch node itself, and then uses
         * the sub-processor returned by this function to process the
         * sub-nodes of the branch.
         * @branch The sub-processor to process the children of
         * that branch.
         */
        fun processBranch(branch: Node.Branch<T>): NodeProcessor<T>

        fun processNode(node: Node<T>): Unit = when (node) {
            is Node.Leaf<T> -> this.processLeaf(node)
            is Node.Branch<T> -> this.processBranch(node).let { subProcessor ->
                node.nodes.forEach(subProcessor::processNode)
            }
        }

    }

}


/**
 * BranchBuilder uses an [ArrayList] to store its (unprocessed) nodes and
 * sub-builders, because nodes will usually only be added sequentially,
 * so the alternative [java.lang.LinkedList] doesn't offer any real performance
 * benefits.
 */
class BranchBuilder<T : Action> internal constructor(var text: String) {
    private val rawNodes: ArrayList<Either<T, BranchBuilder<T>>> = ArrayList()

    fun leaf(action: T) {
        rawNodes.add(Either.Left(action))
    }

    /**
     * Syntactic sugar for adding multiple leaves, or a list of
     * leaves at once. May offer performance benefits for
     * a large number of leaves, since [ArrayList.addAll] is used.
     */
    fun leaves(vararg actions: T) {
        rawNodes.addAll(actions.map { Either.Left(it) })
    }

    // This subroutine is a public method because only this way
    // can branch(String, block) be an inline method.
    fun branch(text: String): BranchBuilder<T> =
        BranchBuilder<T>(text).also { bb -> rawNodes.add(Either.Right(bb)) }

    inline fun branch(text: String, block: BranchBuilder<T>.() -> Unit): BranchBuilder<T> =
        branch(text).also(block)

    fun build(): ActionTree.Node.Branch<T> = ActionTree.Node.Branch(text,
        rawNodes.map { rawNode ->
            when(rawNode) {
                is Either.Left<T, *> -> ActionTree.Node.Leaf(rawNode.value)
                is Either.Right<*, BranchBuilder<T>> -> rawNode.value.build()
            }
        }
    )
}


fun <T : Action> actionTree(action: T) = ActionTree<T>(ActionTree.Node.Leaf(action))

/**
 * Initiates the ActionTree builder DSL.
 *
 * It follows the general pattern of Fusion, and uses a `BranchBuilder`
 * internally to build the branches of the [ActionTree] output.
 */
fun <T : Action> actionTree(text: String, block: BranchBuilder<T>.() -> Unit): ActionTree<T> =
    ActionTree(BranchBuilder<T>(text).also(block).build())
