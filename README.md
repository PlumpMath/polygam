# polygam

TL;DR:

> Choose as many spouses as you want according to your taste.

![polygyny](img/polygyny.jpeg)

This description above (about spouses) is actually rather accurate but it would be easier to explain
it with trees, colours and arrow.

You are given two trees (aka directed acyclic connected graphs), A and B. Each
vertex of tree A has veen give some 'gutt', that's to say a set of logical
rules. The gutt of A defines which vertices of B are available for it. In the
same way, each vertex of B has some gutt which define which vertices of A are
available for it.

Let me say it another way: a vertex from a tree is somehow linked to some set of
vertices of the other tree. A vertex and its linked set can't be from the same
tree.

My goal in this project is to write such a function `availableo` which takes a
vertex from a tree and returns all available vertices of the other tree. It's
really a perfect playground for logic programming.

This project currently barely stands as a draft which I'm working on during my
spare time. I've found useful to see this problem from different viewpoints,
hence you can still find vocabulary about genealogy, graph, match-making, logic
and relational algebra.

## Content of this document

* Literate introduction
* Work path
* Thanks
* License

I've found both necessary and interesting to keep writing documentation to my
side projects as it helps to define more clearly your goals and where you are in
relation to them. The [changelog](CHANGELOG.org) aims at keeping an up-to-date
history of what has been done and what is left.

Once redacted, this document will introduce the problem and the tools I've
defined through a running example. This kind of literate programming should make
very easy to understand the code. However, this is not an introduction from
scratch. You can look up on the fly for the definition of things you don't know:

* What is [a graph](https://en.wikipedia.org/wiki/Graph_(discrete_mathematics))
* Learning [logic programming](https://mitpress.mit.edu/books/reasoned-schemer)
* Getting started with [`core.logic`](https://github.com/swannodette/logic-tutorial) ([code here](https://github.com/clojure/core.logic))

## Tools

In this section we define the current semantic of goals then we give some
examples of expected behaviours.

### Definition

Consider the following tree, which arrows are from top to bottom.

```
             *
             a
            / \
           /   \
          /     \
         /       \
        /         \
       /           \
      /             \
      *              *
      b              c
     / \            / \
    /   \          /   \
   /     \        /     \
   |      |      /       *
   |      |     /        f
   *      |    *       / | \
   d      |    e      /  |  \
  / \     |    |     /   |   \
 *   *    *    *    *    *    *
 g   h    i    j    k    l    m
```

Each arrow stands for the parent <~> child relation. It's defined in the code by
the relation `child`. We can extend this relation to ascendant <~> descandant:
it's the relational goal `kino`. Logic goal are used to be suffixed with a
superscript `o` (or another vowel), which we render in the code by a standard
letter `o`. The name `kino` comes from English nouns kin and kinship which
denotes such a relationship. Last word about the family, you can access siblings
of a vertex (other children of the same parents different from the given vertex)
by goal `siblingso`.

As we're talking about a tree, it may be useful to later be able to deal
specifically with it. Let's get any vertex which have no parents (aka a root)
with goal `tope` (from top) and any vertex without children with `leafo` (from
leaf). An easy combination of the two latter goals gives `boundaryo` (from
boundary). Although the boundary of a a tree is not really worth a definition,
perhaps it will prove itself useful later.

Vertices can be elicited or rejected. Two relations embodie this is the code.
Their names are pretty straightforward as `yap` convey a positive meaning
(chosen amongst siblings) whilst `yuk` sounds negatively yucky. Ikks!

Even if `yap` and `yuk` can't be used together yet in a coherent fashion, you
can still manipulate them separately with two goals `yap-treeo` and `yuk-treeo`.
The first one can return any vertex which has been elicited (yapped) or any
child of such a vertex. It means that ignored vertices, that's to say non-yapped
siblings of a yapped vertex can not be comprehended by `yap-treeo`. By the way,
forgive my poor ability to name things in proper English but such an ignored
vertex is said to be empeached. You can reach it through the relational goal
`empeachedo`.

`yuk-treeo` is the symetric goal which can accept any rejected (yukked) vertex
of any children of such a vertex.

Some subtleties still have to be defined when you intertwinned yukked and yapped
vertices in a tree.

### Strategy to find the set of available vertices for a given node

#### Define what is a vertex
#### Define the child-parent relation between two vertices
#### Define gutt functions for what is rejected and what is elicited
#### Define the descendants of a rejected node
#### Define the descendants of an elicited node
#### Precise what it means to be a root of a graph, that's to say to have no parents
#### Precise what it means to be a leaf of a graph, that's to say to have no children
#### Define what is being impeached
#### Explicit when a vertex both child of a rejected / impeached node and an
  elicited node can be available or not

### Step-by-step examples

![polygyny](doc/graph-sample.png)

In this section, the relation `availableo` is constructed by step. Each step
takes builds on the result of the previous one.

The early version of this relation `availablero` binds the value of a logic
variable to all nodes which can are available for a given gutt and only these
nodes. How to get such a relation?

#### Step -1: Anything

    (run* [q])

`=> (_0)` because there is no rule of constraint on the variable.

#### Step 0: all vertices

We use the facts defined in `definitions` as the definition of what a vertex is.

    (with-dbs [definitions]
      (run* [q]
        (vertex q)))

`=> (:a :b :c :d :e :f :g :h :i :j :k :l :m :n :o :p :q)` all nodes are here.
I've added an implicit call to `sort` to make the result more human friendly.

#### Step 1: only keep vertices which are not explicitly rejected

Let's bind the logic variable to any node which has been explicitly rejected:

    (with-dbs [definitions favour]
      (run* [q]
        (yuk q))))

`=> (:b :h)` it seems to work well, let's go further and use _negation as a
failure_ to retrieve all the values for which it's impossible to match the goal
`yuk`.

    (with-dbs [definitions favour]
      (run* [q]
        (nafc yuk q))))

outputs something like `=> (_0)` because the logic variable can be anything,
provided that *anything* doesn't match the goal `yuk`. You need to narrow the
possible values of the logic variable to all vertices:

    (with-dbs [definitions favour]
      (run* [q]
        (vertex q)
        (nafc yuk q))))

`=> (:a :c :d :e :f :g :i :j :k :l :m :n :o :p :q)` from which `:b` and `:h`
have been removed.

#### Step 2: go through sub-steps

A soft-cut strategy is applied: ask each possible value the question of the
sub-step: if it's a match then conditions apply and further questions are
ignored; if it's a miss, go to next step and repeat the process. If no sub-steps
are valid it fails. If one step is valid then further steps are ignored.

See `condo` in the Reasoned Schemer of `conda` in `core.logic`.

The final form of this step can be found in the code. Only simple sub-steps are
shown here.

#### Step 2.0: has this vertex been explicitly chosen?

    (with-dbs [definitions favour]
      (run* [q]
        (yap q))))

`=> (:a :c :j :k :n)`

If yes, it succeeds without any conditions. Here, it means the logic variable
the goal `availableo` is dealing with will be able to take the value which
succeeds here.

#### Step 2.1: is this vertex free from impeachment?

    (with-dbs [definitions favour]
      (run* [q]
        (vertex q)
        (nafc impeachedo q)))

If yes, it will succeed if and only if it's not a descandant of an explicitly
rejected node. In equivalent terms, it will succeed if and only if it's
impossible to find an explicitly rejected vertex whose this node descend from.

#### Step 2.2: is this vertex impeached?

    (with-dbs [definitions favour]
      (run* [q]
        (vertex q)
        (impeachedo q)))

If yes, it's a fail.

#### Step 2.3: is this vertex son of both a rejected / impeached node and an elicited node?

The following code has been wrapped into a named goal: `son-of-yap-son-of-yuko`.

    (with-dbs [definitions favour kin]
      (run* [q]
        (fresh [a b]
          (kino a q)
          (kino a b)
          (kino b q)
          (yuk a)
          (yap b)
          (l/!= a q)
          (l/!= a b)
          (l/!= b q))))

If yes, it succeeds.

**TODO: infortunately, the current graph sampe doesn't show this case**

#### Step 2.4: is it impossible to find an explicitly rejected vertex whose this node descend from?

    (with-dbs [definitions favour kin]
      (run* [q]
        (vertex q)
        (nafc yuk-treeo q)))

If yes, it succeeds.

#### Put everything together

### Examples

Consider the annotated draw of the same previous graph.

> **__Warning, figure has got uploaded, text is outdated__**

![graph](./doc/graph-sample.png)

Vertices a, c and d and e have been added some marks. Nodes a, d and e are
marked with O, meaning they have been elicited (yapped). Node c is rejected,
hence given an X.

Let's describe the expected behaviour of aforedefined goals:

 * `yap-treeo` should allow vertices a, b, c, d, e, g, h, j. Vertex f and its
   children k, l, m are ignored because its sibling e has been elicited
   (yapped). Vertex i is ignored because of the same reason.
 * `empeachedo` should hence return the previous ignores vertices: i, k, l and
   finally m.
 * `yuk-treeo` should match vertices c, e, f, j, k, l, m because c is rejected
   (yukked) and other nodes are children of it.

When looking for general answer, which are available nodes to be chosen?
Rejected (yukked) vertex c is child of elicited vertex a; j is direct child of
elicited (yapped) vertex e and the latter is direct child of rejected (yukked)
vertex c. Because of this setting, available nodes should be a, b, d, e, g, h,
j. Vertex c is removed because explicit rejection. Vertices e and j are
descendants of excluded node c but have a nearer elicited parent so they are
kept available.

## Work path

> Divide and conquer

![graph](http://scholasticadministrator.typepad.com/.a/6a00e54f8c25c988340163033d2321970d-200wi)

Methinks compulsory to break this problem into smaller pieces that can fit my
brain. Here are the ordained list of foreseen steps toward general resolution.

- Forget the two graphs and the like. Just take one single graph and write
  relations to get ancestors, descendants, siblings and so on.
- Let's introduce two constraints, yap and yuk. Yap means this node has been
  elicited amongst its siblings so the latter are discarded in favour of any
  yapped node. Yuk is the symetric: any yucky yukked node is belittled in favour
  of any other siblings. Let's define and play with these constraints
  separately.
- Once you apply yap on a graph, some nodes get a situation of impeachment to
  deal with. The relation which give all available nodes doesn't have to be
  mind-blowing but it surely will need to be carefully crafted.
- Yap and yuk applied together add another situation of precedence. If your
  parent is yukked but you're yapped, you and your descendants should be
  available but your siblings shouldn't.
- You can add the other graph now. Make careful, small steps not to get lost.
  First of it is a naming rule: graph A is the graph whose vertices are to be
  chosen, graph B is the graph whose vertices have tastes.
- Vertices of B only have their own tastes and nothing else.
- Vertices of B have parents. They inherit their parents' tastes.
- Keep your code in full Clojure but use ClojureScript to build a simple visual
  web interface to this.
- OK, now go and write a memoir about this ^^

## Thanks

* William E. Byrd
* Any other people cited in his dissertation

## License

Copyright © 2016 piotr-yuxuan

Distributed under the GNU General Public License either version 3.0 or (at your
option) any later version.
