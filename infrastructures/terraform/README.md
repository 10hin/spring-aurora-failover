# terraformコードについて

## 汎用性の高いリソースのlocal変数化

`aws_partition`/`aws_region`/`aws_account_id`は多くのリソースで利用するのでlocal変数化した。

## `eksctl`で作成したリソースの参照

VPC、サブネット、EKSクラスタに設定されるSecurityGroup等はData sourceを使って参照している。

## 利用するAvailability zone(AZ)数とサブネットについて

AZは、現在のリージョンで利用可能なAZからlocal変数`aws_az_count`の値のぶんだけを利用する。AZは名前の辞書順(東京リージョンなら多くのAWSアカウントで`ap-norteast-1a`、`ap-northeast-1c`、`ap-northeast-1d`の順)でソートして先頭から必要な個数をとる。

>
> #### 制限事項
>
> `eksctl`でサブネットを作成するときに必要なAZにサブネットを作成している必要がある。
> 例えば、`aws_az_count`を`2`にセットして`eksctl`では`ap-northeast-1c`/`ap-northeast-1d`しか利用していないと失敗する。
>

## Auroraインスタンス数とインスタンスのAZ配置について

local変数`aurora_instance_count`の値のぶんだけAuroraインスタンスを作成する。

インスタンスは`01`、`02`、…とsuffixをつけ、利用するAZをAZの名前順で利用する。

>
> #### 例
>
> `aws_az_count`が2、`aurora_instance_count`が3で`aws_region`が`ap-northeast-1`、利用可能なサブネットが`ap-northeast-1a`/`ap-northeast-1c`/`ap-northeast-1d`とすると、suffixとその作成されるAZの関係は以下の表のとおり
>
> | suffix | AZ | 補足 |
> |:------:|:--:|:-----|
> | `01`   | `ap-northeast-1a` | `...-1a`から利用する |
> | `02`   | `ap-northeast-1c` |  |
> | `03`   | `ap-northeast-1a` | `aurora_instance_count > aws_az_count`なので一周して戻ってくる |
>
>
