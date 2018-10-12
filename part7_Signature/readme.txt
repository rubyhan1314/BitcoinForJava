

step1：修改TxOutput类


step2：修改TxInput类

step3：修改Transaction类

    修改newUTXOTransaction()
    添加getData()
    添加trimmedCopy()
    添加sign()
    添加verify()



step4：修改Blockchain类

    A：修改getAllSpentTXOs()

    B :修改findUnspentTransactions()

    C：修改findUTXO()

    D:修改findSpendableOutputs()

    E:添加方法findTransaction()


    F：添加signTransaction()

    G:添加verifyTransactions()

    H:修改mineBlock()，添加验证

step5：修改CLI


 最后修改blockchain.sh脚本文件