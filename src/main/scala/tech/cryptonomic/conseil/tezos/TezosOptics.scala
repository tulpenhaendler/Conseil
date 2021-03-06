package tech.cryptonomic.conseil.tezos

import java.time.ZonedDateTime

import TezosTypes._
import TezosTypes.OperationMetadata.BalanceUpdate
import tech.cryptonomic.conseil.tezos.TezosTypes.Scripted.Contracts
import monocle.std.option._

/** Provides [[http://julien-truffaut.github.io/Monocle/ monocle]] lenses and additional "optics"
  * for most common access and modifcation patterns for Tezos type hierarchies and ADTs
  */
object TezosOptics {

  import monocle.Optional
  import monocle.macros.{GenLens, GenPrism}

  object Blocks {

    //basic building blocks to reach into the block's structure
    val blockData = GenLens[Block](_.data)
    val dataHeader = GenLens[BlockData](_.header)
    val metadata = GenLens[BlockData](_.metadata)
    val headerTimestamp = GenLens[BlockHeader](_.timestamp)
    val metadataType = GenPrism[BlockMetadata, BlockHeaderMetadata]
    val metadataBalances = GenLens[BlockHeaderMetadata](_.balance_updates)

    /** An optional lens allowing to reach into balances for blocks' metadata */
    val blockBalances: Optional[Block, List[BalanceUpdate]] =
      blockData composeLens metadata composePrism metadataType composeLens metadataBalances

    /** a function to set the header timestamp for a block, returning the modified block */
    val setTimestamp: ZonedDateTime => Block => Block = blockData composeLens dataHeader composeLens headerTimestamp set _

    /** a function to set metadata balance updates in a block, returning the modified block */
    val setBalances: List[BalanceUpdate] => Block => Block = blockBalances set _
  }

  object Accounts {

    //basic building blocks to reach into the account's structure
    private val accountScript = GenLens[Account](_.script)

    private val contractsCode = GenLens[Contracts](_.code)

    private val storageCode = GenLens[Contracts](_.storage)

    private val expression = GenLens[Micheline](_.expression)

    /** an optional lens allowing to reach into the script code field of an account*/
    val scriptLens = accountScript composePrism some composeLens contractsCode composeLens expression

    /** an optional lens allowing to reach into the script storage field of an account*/
    val storageLens = accountScript composePrism some composeLens storageCode composeLens expression
  }

}
