# ChorFlow

Information Flow Analysis for Choregraphic Programs

## Instructions

The project has been pre-built and can be run on Linux with `chorflow` and Windows 64-bit with `chorflow.bat`.

The program defines three commands, `check`, `graph`, and `pprint`:

```
Usage: chorflow [OPTIONS] COMMAND [ARGS]...

  Information flow analysis for choreographic programs

Commands:
  check   Check the information flow of a choreography against a flow policy
  graph   Display/save a flow graph for a choreography, a policy, or combined
  pprint  Parse and pretty print a choreography
```

The main command is `check`, which takes in a choreography, a policy, and a flow mapping specification. It uses the mapping to generate the flow for the choreography and checks it against the policy during type checking:

```
Usage: chorflow check [OPTIONS] CHOR POL MAP

  Check the information flow of a choreography against a flow policy

Arguments:
  CHOR  choreography file
  POL   policy file
  MAP   flow mapping file
```

The command `graph` can be used to visualize the flow graphs and/or save them as PNG-files to a directory:

```
Usage: chorflow graph [OPTIONS]

  Display/save a flow graph for a choreography, a policy, or combined

Options:
  -c, --chor PATH  choreography file (must be used with -m/--map)
  -m, --map PATH   flow mapping file (must be used with -c/--chor)
  -p, --pol PATH   policy file
  -d, --display    display flow graph(s)
  -s, --save       save flow graph(s)
  -o, --out PATH   output directory for graph images (default: current folder)
```

To visualize the flow induced by a choreography, supply the choreography and flow mapping options. To visualize a flow policy, supply just the policy option. To get a combined view, provide all three. The choreography flows are shown with solid edges, policy flows with dashed edges, and flows violating the policy are shown with red edges in the combined graph.

Finally, the utility command `pprint` can be used to parse and pretty print a choreography. This is mostly supplied as a sanity check to validate whether your choreographies are correctly formatted:

```
Usage: chorflow pprint [OPTIONS] CHOR

  Parse and pretty print a choreography

Options:
  -i, --indent INT  indentation width (default: 4)
  -c, --condensed   condense to single line
```

## Build

To build the program from source, use the supplied gradle wrapper and run

```bash
$ gradlew installDist
```

The executable `ChorFlow` can then be run from `build/install/ChorFlow/bin/`.

## Syntax

### Choreographies

Choreographies are written using the following context-free grammer:

```
Prog: P C

C : I; C    # Sequential composition
  | 0       # Terminated choreography

I : p.x := e                # Assignment
  | p.e -> q.x              # Interaction
  | p -> q[l]               # Selection
  | if p.e then C else C    # Conditional
  | X(p, q, ...)            # Procedure call

P : X(p, q, ...) = C, P     # Procedure definition
  | ∅                       # Nothing
```

A program consists of an optional list of comma-separated procedure definitions, followed by a main choreography. A choreography is a semicolon-separated sequence of instructions and ends in `0`.  Comments can be added inline with `//`, multiline comments with `/* ... */`, and newlines/indentation can be used to make the program more readable.

As an example, consider two processes, `s1` and `s2`, collaborating on finding a counterexample to the Collatz Conjecture. Starting from `n = 1` they take turns checking the next `n` to avoid overheating:

```
Search(p, q) =
    if p.collatz(n) == 1 then
        // Swap roles and continue with n + 1
        p -> q[ok];
        p.(n + 1) -> q.n;
        Search(q, p);
        0
    else
        // Counterexample found, pass it on
        p -> q[end];
        p.n -> q.n;
        0
    ; 0

s1.n := 1;
Search(s1, s2);
0
```

This has a single procedure `Search` which calls itself recursively until finding a counterexample, swapping the process order for each call. The main choreography simply initializes a value and invokes the procedure.

### Policies

Policies are specified with the following syntax (comments start with `#`), where `p` is a process and `p.x` a variable located at `p`:

```
# Allow flow from p to q, s, and t
p -> q
p -> s
p -> t

# Shorthand for above
p -> q, s, t

# Data flow (allows flow from x -> y, not e.g. p -> y or x -> q)
p.x -> q.y
```

In the above, no flow is allowed from `q`. The following syntax can be used to show this is intentional and not an oversight:

```
# Show no flow from q is allowed
q ->
```

This is strictly syntactical and has no effect. In particular, it does not *disallow* flow, so if `q -> p` is also specified somewhere in the policy, then flow from `q` to `p` *is* allowed.


### Flow Mappings

Flow mappings define what flows each type of choreography *event* induces. This depends on the use case and can be specified with high granularity. The following template can be used as a starting point and generates all possible flow for each event:

```
# Conditional
flow(p.e) = p <-> e

# Assignment
flow(p.x := e) = p <-> x, p <-> e, x <-> e

# Selection
flow(p -> q[L]) = p <-> q

# Interaction
flow(p.e -> q.x) = p <-> e, p <-> q, p <-> x, e <-> q, e <-> x, q <-> x
```

Consider as an example the following mapping:

```
flow(p.x := e) = e -> x
flow(p.e -> q.x) = e -> x
```

This mapping ignores all control flow between processes and focuses only on the explicit data flow induced when an expression is evaluated and the result stored. If the choreography is

```
p.x := a;
if p.(x == 42) then
    p."yes" -> q.y;
else
    p."no" -> q.y;
; 0
```

then the flow induced with this mapping is just

```
p.a -> p.x
```

and this is what is checked against a policy.

In this example, we can infer whether `p.a == 42` based just on what `q.y` ends up to be, so there is clearly also an implicit flow, we may or may not care about. If we changed the mapping to be

```
flow(p.e) = e -> p
flow(p.x := e) = e -> x
flow(p.e -> q.x) = e -> x, p -> q, q -> x
```

then the flow induced would instead be

```
p.a -> p.x
p.x -> p
p -> q
q -> q.y
```

and give us the path `p.a -> p.x -> p -> q -> q.y` in the flow graph, revealing the implicit flow. To disallow this flow, at least one of these links must be absent in the policy.

## Examples

A few example choreographies and corresponding policies can be found in `examples/`. Flow mappings can be created from `template.map` in `flowmaps/` where a few sample mappings can be found as well.


Example with no flow violations:

```bash
$ chorflow check examples/OpenID/OpenID.chor examples/OpenID/policy-implicit.flow flowmaps/explicit-implicit-flow.map
```

Same flow violates a stricter policy:

```bash
$ chorflow check examples/OpenID/OpenID.chor examples/OpenID/policy-explicit.flow flowmaps/explicit-implicit-flow.map
```

but not if only the explicit data flows are induced by the mapping:

```bash
$ chorflow check examples/OpenID/OpenID.chor examples/OpenID/policy-explicit.flow flowmaps/explicit-data-flow.map
```

Visualize a policy flow:

```bash
$ chorflow graph --pol examples/OpenID/policy-explicit.flow --display
```

Visualize a choreography flow based on a mapping:

```bash
$ chorflow graph --chor examples/OpenID/OpenID.chor --map flowmaps/explicit-data-flow.map --display
```

Get a combined view of choreographic flow and policy, saving the results to the folder `graphs/` (folder must exist):

```bash
$ chorflow graph --chor examples/OpenID/OpenID.chor --pol examples/OpenID/policy-explicit.flow --map flowmaps/explicit-data-flow.map --save --out graphs
```

Pretty print a choreography, indenting using 2 spaces:

```bash
$ chorflow pprint examples/OpenID/OpenID.chor --indent 2
```
